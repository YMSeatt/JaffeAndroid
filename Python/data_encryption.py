"""
data_encryption.py: Fernet-based encryption layer for classroom data.

This module provides utility functions for securing JSON data files using
the Fernet symmetric encryption specification. It is designed to maintain
logical parity with the Android application's security utilities, enabling
seamless data exchange between platforms.

Security Note:
The implementation currently relies on a shared 32-byte key imported from
`encryption_key.py`. In production environments, this key must be kept secret
and never committed to public version control.
"""

from cryptography.fernet import Fernet
import os
from encryption_key import encryption_key as ENCRYPTION_KEY
import json
import cryptography.fernet

# Initialize the Fernet cipher suite using the shared encryption key.
# This instance is used for all encryption/decryption operations within the module.
f = Fernet(ENCRYPTION_KEY)

# --- Encryption and Decryption Functions ---

def encrypt_data(data_string):
    """
    Encrypts a plaintext string into a Fernet token.

    :param data_string: The raw string to encrypt (typically a JSON string).
    :return: A URL-safe base64-encoded Fernet token as bytes.
    """
    encrypted_data = f.encrypt(data_string.encode('utf-8'))
    return encrypted_data

def decrypt_data(encrypted_data):
    """
    Decrypts a Fernet token back into a plaintext string.

    :param encrypted_data: The Fernet token bytes to decrypt.
    :return: The decrypted plaintext string.
    :raises cryptography.fernet.InvalidToken: If the token is invalid or the key is incorrect.
    """
    decrypted_data = f.decrypt(encrypted_data).decode('utf-8')
    return decrypted_data

def _read_and_decrypt_file(file_path):
    """
    Reads a file from disk, attempts to decrypt its content, and parses it as JSON.

    This function implements a "safe" decryption strategy:
    1. It first attempts to decrypt the file using the standard Fernet logic.
    2. If decryption fails (InvalidToken), it assumes the file might be stored
       as plaintext (backward compatibility or user-disabled encryption) and
       attempts to decode it as a UTF-8 string.
    3. Finally, it parses the resulting string as JSON.

    :param file_path: The path to the file to read.
    :return: The deserialized JSON data (dict/list) or None if the file is missing/empty.
    :raises json.JSONDecodeError: If the file content is not valid JSON.
    """
    if not os.path.exists(file_path):
        return None
    try:
        with open(file_path, 'rb') as f_read:
            file_content = f_read.read()

        if not file_content: # File is empty
            return None

        try:
            # Attempt to decrypt first
            decrypted_data_string = decrypt_data(file_content)
        except cryptography.fernet.InvalidToken:
            # If decryption fails, it's likely plaintext (or corrupt)
            # Assume it's a UTF-8 encoded string.
            decrypted_data_string = file_content.decode('utf-8')

        return json.loads(decrypted_data_string)

    except (json.JSONDecodeError, IOError, UnicodeDecodeError) as e:
        print(f"Error loading and decoding file {os.path.basename(file_path)}: {e}")
        return None
        
def _encrypt_and_write_file(file_path, data_to_write, rule):
    """
    Encodes application data to JSON and writes it to a file, optionally encrypted.

    :param file_path: The destination path for the file.
    :param data_to_write: The data object (dict or list) to serialize.
    :param rule: A boolean indicating whether to apply encryption (True)
                 or save as plaintext (False).
    """
    try:
        json_data_string = json.dumps(data_to_write, indent=4)

        # Determine whether to encrypt based on the 'rule' parameter.
        if rule:
            data_to_write_bytes = encrypt_data(json_data_string)
        else:
            data_to_write_bytes = json_data_string.encode('utf-8')

        with open(file_path, 'wb') as f:
            f.write(data_to_write_bytes)

    except IOError as e:
        print(f"Error saving file {os.path.basename(file_path)}: {e}")
    except Exception as e:
        print(f"An unexpected error occurred while saving {os.path.basename(file_path)}: {e}")