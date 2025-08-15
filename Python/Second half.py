
    def export_data_to_excel(self, file_path, export_format="xlsx", filter_settings=None, is_autosave=False, export_all_students_info = True):
        # ... (substantially updated for new log types, summaries, and filtering)
        wb = Workbook()
        wb.remove(wb.active) # Remove default sheet
        mark_type_configs = self.settings.get("quiz_mark_types", [])
        mark_type_configs_h = self.settings.get("homework_mark_types", [])
        quiz_mark_type_headers = [mt["name"] for mt in mark_type_configs]
        homework_mark_type_headers = [mt["name"] for mt in mark_type_configs_h]
        homework_session_types_headers = [mt["name"] for mt in self.all_homework_session_types]

        student_data_for_export = {sid: {"first_name": s["first_name"], "last_name": s["last_name"], "full_name": s["full_name"]} for sid, s in self.students.items()}
        
        logs_to_process = []
        if filter_settings.get("include_behavior_logs", True):
            logs_to_process.extend([log for log in self.behavior_log if log.get("type") == "behavior"])
        if filter_settings.get("include_quiz_logs", True):
            logs_to_process.extend([log for log in self.behavior_log if log.get("type") == "quiz"])
        if filter_settings.get("include_homework_logs", True): # New
            logs_to_process.extend([log for log in self.homework_log if log.get("type") == "homework" or log.get("type") == "homework_session_y" or log.get("type") == "homework_session_s"])

        # Apply filters
        filtered_stud_ids = set()
        filtered_log = []
        start_date = filter_settings.get("start_date")
        end_date = filter_settings.get("end_date")
        selected_students_option = filter_settings.get("selected_students", "all")
        student_ids_filter = filter_settings.get("student_ids", [])
        selected_behaviors_option = filter_settings.get("selected_behaviors", "all")
        behaviors_list_filter = filter_settings.get("behaviors_list", [])
        selected_homework_types_option = filter_settings.get("selected_homework_types", "all") # New
        homework_types_list_filter = filter_settings.get("homework_types_list", []) # New
        for entry in logs_to_process:
            try:
                entry_date = datetime.fromisoformat(entry["timestamp"]).date()
                if start_date and entry_date < start_date: continue
                if end_date and entry_date > end_date: continue
            except ValueError: continue # Skip if timestamp is invalid

            if selected_students_option == "specific" and entry["student_id"] not in student_ids_filter: continue
            filtered_stud_ids.add(entry["student_id"])
            log_type = entry.get("type", "behavior")
            
            entry_name_field = entry.get("behavior") # Default for behavior and quiz
            if log_type == "homework" or log_type == "homework_session_y" or log_type == "homework_session_s":
                entry_name_field = entry.get("homework_type", entry.get("behavior")) # For homework logs
                #entry_name_field2 = entry.get("home")
            #print("list", homework_types_list_filter)
            #print("entry", entry_name_field)
            if log_type == "behavior" or log_type == "quiz":
                if selected_behaviors_option == "specific" and entry_name_field not in behaviors_list_filter: continue
            elif log_type == "homework" or log_type == "homework_session_s":
                if selected_homework_types_option == "specific" and entry_name_field not in homework_types_list_filter: continue
                elif selected_homework_types_option == "specific" and entry_name_field in homework_types_list_filter: continue
            elif log_type == "homework_session_y":
                if selected_homework_types_option == "specific" and entry_name_field not in homework_types_list_filter: continue#continue
                elif selected_homework_types_option == "specific" and entry_name_field in homework_types_list_filter: pass
            filtered_log.append(entry)
        
        filtered_log.sort(key=lambda x: x["timestamp"])

        # Determine sheet strategy
        separate_sheets = filter_settings.get("separate_sheets_by_log_type", True) # type: ignore
        master_log = filter_settings.get("include_master_log", True) if separate_sheets else False
        sheets_data = {} # {sheet_name: [entries]}

        if separate_sheets and not master_log:
            if filter_settings.get("include_behavior_logs", True): sheets_data["Behavior Log"] = []
            if filter_settings.get("include_quiz_logs", True): sheets_data["Quiz Log"] = []
            if filter_settings.get("include_homework_logs", True): sheets_data["Homework Log"] = [] # New
            for entry in filtered_log:
                log_type = entry.get("type")
                if log_type == "behavior" and "Behavior Log" in sheets_data: sheets_data["Behavior Log"].append(entry)
                elif log_type == "quiz" and "Quiz Log" in sheets_data: sheets_data["Quiz Log"].append(entry)
                elif (log_type == "homework" or log_type == "homework_session_y" or log_type == "homework_session_s") and "Homework Log" in sheets_data: sheets_data["Homework Log"].append(entry)
        elif separate_sheets and master_log:
            if filter_settings.get("include_behavior_logs", True): sheets_data["Behavior Log"] = []
            if filter_settings.get("include_quiz_logs", True): sheets_data["Quiz Log"] = []
            if filter_settings.get("include_homework_logs", True): sheets_data["Homework Log"] = [] # New
            if filter_settings.get("include_master_log", True): sheets_data["Master Log"] = [] # Newer
            for entry in filtered_log:
                log_type = entry.get("type")
                if log_type == "behavior" and "Behavior Log" in sheets_data: sheets_data["Behavior Log"].append(entry)
                elif log_type == "quiz" and "Quiz Log" in sheets_data: sheets_data["Quiz Log"].append(entry)
                elif (log_type == "homework" or log_type == "homework_session_y" or log_type == "homework_session_s") and "Homework Log" in sheets_data: sheets_data["Homework Log"].append(entry)
            sheets_data["Master Log"] = filtered_log
        else:
            sheets_data["Combined Log"] = filtered_log


        bold_font = OpenpyxlFont(bold=True)
        center_alignment = OpenpyxlAlignment(horizontal='center', vertical='center', wrap_text=True)
        left_alignment = OpenpyxlAlignment(horizontal='left', vertical='center', wrap_text=True)
        right_alignment = OpenpyxlAlignment(horizontal='right', vertical='center', wrap_text=False)

        for sheet_name, entries_for_sheet in sheets_data.items():
            if not entries_for_sheet and ((sheet_name != "Combined Log" or sheet_name != "Master Log") or not filtered_log) : continue # Skip empty specific sheets

            ws = wb.create_sheet(title=sheet_name)
            headers = ["Timestamp", "Date", "Time", "Day", "Student ID", "First Name", "Last Name"]
            if sheet_name == "Behavior Log" or not separate_sheets or sheet_name == "Master Log": headers.append("Behavior")
            if sheet_name == "Quiz Log" or not separate_sheets or sheet_name == "Master Log":
                headers.extend(["Quiz Name", "Num Questions"])
                # Add headers for each mark type (e.g., Correct, Incorrect, Bonus)
                for mt in self.settings.get("quiz_mark_types", []): headers.append(mt["name"])
                headers.append("Quiz Score (%)")
            if sheet_name == "Homework Log" or not separate_sheets or sheet_name == "Master Log": # New headers for Homework
                headers.extend(["Homework Type/Session Name", "Num Items"])
                # Add headers for each homework mark type
                for hmt in self.settings.get("homework_mark_types", []): headers.append(hmt["name"])
                headers.extend(["Homework Score (Total Pts)", "Homework Effort"]) # Example summary fields
                headers.extend(homework_session_types_headers)
            headers.append("Comment")
            i=0
            for header in headers: 
                if header == "Complete": headers[i] = "Complete/Did"
                i += 1
            if not separate_sheets or sheet_name == "Master Log": headers.append("Log Type")


            for col_num, header_title in enumerate(headers, 1):
                cell = ws.cell(row=1, column=col_num, value=header_title)
                cell.font = bold_font; cell.alignment = center_alignment
            ws.freeze_panes = 'A2'

            row_num = 2
            for entry in entries_for_sheet:
                student_info = student_data_for_export.get(entry["student_id"], {"first_name": "N/A", "last_name": "N/A"})
                try: dt_obj = datetime.fromisoformat(entry["timestamp"])
                except ValueError: dt_obj = datetime.now() # Fallback
                col_num = 1
                ws.cell(row=row_num, column=col_num, value=entry["timestamp"]); col_num+=1
                ws.cell(row=row_num, column=col_num, value=dt_obj.strftime('%Y-%m-%d')).alignment = right_alignment; col_num+=1
                ws.cell(row=row_num, column=col_num, value=dt_obj.strftime('%H:%M:%S')).alignment = right_alignment; col_num+=1
                ws.cell(row=row_num, column=col_num, value=entry.get("day", dt_obj.strftime('%A'))); col_num+=1
                ws.cell(row=row_num, column=col_num, value=entry["student_id"]); col_num+=1
                ws.cell(row=row_num, column=col_num, value=student_info["first_name"]); col_num+=1
                ws.cell(row=row_num, column=col_num, value=student_info["last_name"]); col_num+=1

                entry_type = entry.get("type", "behavior")

                if sheet_name == "Behavior Log" or ((not separate_sheets or sheet_name == "Master Log") and entry_type == "behavior"):
                    ws.cell(row=row_num, column=col_num, value=entry.get("behavior")); col_num+=1
                elif sheet_name == "Quiz Log" or ((not separate_sheets or sheet_name == "Master Log") and entry_type == "quiz"):
                    ws.cell(row=row_num, column=col_num, value=entry.get("behavior")); col_num+=1 # Quiz Name
                    num_q = entry.get("num_questions", 0); ws.cell(row=row_num, column=col_num, value=num_q).alignment = right_alignment; col_num+=1
                    marks_data = entry.get("marks_data", {})
                    total_possible_points_for_calc = 0; total_earned_points_for_calc = 0; extra_credit_earned = 0
                    for mt in self.settings.get("quiz_mark_types", []):
                        points = marks_data.get(mt["id"], 0)
                        ws.cell(row=row_num, column=col_num, value=points).alignment = right_alignment; col_num+=1
                        if mt.get("contributes_to_total", True): total_possible_points_for_calc += mt.get("default_points",1) * num_q # Simplified: assumes each question can get this mark type
                        if points > 0 : # Only add earned if student got this mark
                            if mt.get("is_extra_credit", False): extra_credit_earned += points * mt.get("default_points",1)
                            else: total_earned_points_for_calc += points * mt.get("default_points",1)
                    # More robust score calculation needed based on how num_questions and marks_data relate
                    score_percent = 0
                    if num_q > 0: # Use num_questions as the basis for total possible points from main Qs
                        # Calculate total possible for main questions based on default points of contributing mark types
                        # This is a simplification; assumes each question has a potential max based on one 'correct' type
                        main_q_total_possible = 0
                        correct_type = next((m for m in self.settings.get("quiz_mark_types",[]) if m.get("id") == "mark_correct"), None)
                        if correct_type: main_q_total_possible = correct_type.get("default_points", 1) * num_q

                        if main_q_total_possible > 0:
                            score_percent = ((total_earned_points_for_calc + extra_credit_earned) / main_q_total_possible) * 100
                        elif total_earned_points_for_calc + extra_credit_earned > 0 : # Scored only on EC or non-standard
                            score_percent = 100 # Or some other representation
                    ws.cell(row=row_num, column=col_num, value=round(score_percent,2) if score_percent else "").alignment = right_alignment; col_num+=1
                elif sheet_name == "Homework Log" or ((not separate_sheets or sheet_name == "Master Log") and (entry_type == "homework" or entry_type == "homework_session_y" or entry_type == "homework_session_s")): # New Homework
                    ws.cell(row=row_num, column=col_num, value=entry.get("homework_type", entry.get("behavior"))); col_num+=1 # Homework Type/Session Name
                    num_items = entry.get("num_items") # For manually logged with marks
                    if entry.get("type") == "homework_session_s": # For live sessions
                        # Try to count items from details if Yes/No mode
                        homework_details = entry.get("homework_details", {})
                        if not is_autosave:
                            num_items = len(homework_details.get("selected_options",[])) if isinstance(homework_details, dict) else 0
                    elif entry.get("type") == "homework_session_y":
                        num_items = None
                    
                    if separate_sheets and (sheet_name == "Combined Log") or sheet_name == "Master Log":
                        col_num += len(headers)-(len(homework_session_types_headers))-(col_num)-10
                    ws.cell(row=row_num, column=col_num, value=num_items if num_items is not None else "").alignment = right_alignment; col_num+=1
                    total_hw_points = 0; effort_score_val = "" # For summary columns
                    if entry_type == "homework" and "marks_data" in entry: # Graded manual log
                        
                        #if separate_sheets and (sheet_name == "Combined Log") or sheet_name == "Master Log":
                        #    col_num += len(headers)-(len(homework_session_types_headers))-(col_num)-9
                        
                        hw_marks_data = entry.get("marks_data", {})
                        for hmt in self.settings.get("homework_mark_types", []):
                            val = hw_marks_data.get(hmt["id"], "")
                            ws.cell(row=row_num, column=col_num, value=val).alignment = right_alignment; col_num+=1
                            if isinstance(val, (int,float)): total_hw_points += val # Sum points if numeric
                            if hmt["id"] == "hmark_effort": effort_score_val = val # Capture effort score
                            
                    elif entry_type == "homework_session_s" or entry_type == "homework_session_y": # Live session log
                        # For live sessions, fill placeholders for mark type columns or try to map
                        session_details = entry.get("homework_details", {})
                        live_session_mode = entry.get("type")
                        if live_session_mode == "homework_session_y":
                            """
                            for hmt in self.settings.get("homework_mark_types", []): # Fill placeholders
                                # Could try to map "Yes" to complete, "No" to not done, etc.
                                # For now, just leave blank or show raw status if one of the types matches the key
                                found_status_for_mark_type = ""
                                for hw_type_id_key, status_val in session_details.items():
                                    # This mapping is very approximate.
                                    #print(status_val)
                                    if hmt["name"].lower() in hw_type_id_key.lower() or hmt["name"].lower() == status_val.lower():
                                        found_status_for_mark_type = status_val
                                        break
                                    elif "complete" in hmt["name"].lower() and status_val.lower() == "yes":
                                        found_status_for_mark_type = "Yes" # Or map to points
                                        if "default_points" in hmt: total_hw_points += hmt["default_points"]
                                        break
                                    elif "not done" in hmt["name"].lower() and status_val.lower() == "no":
                                        found_status_for_mark_type = "No"
                                        if "default_points" in hmt: total_hw_points += hmt["default_points"]
                                        break
                                ws.cell(row=row_num, column=col_num, value=found_status_for_mark_type).alignment = right_alignment; col_num+=1
                            """
                            i = 0
                            #print(self.all_homework_session_types)
                            found_status_for_mark_type2 = ""
                            col_num += ((((len(headers)-col_num)-len(homework_session_types_headers))) if not is_autosave else (((len(headers)-col_num)-len(homework_session_types_headers)))) if "Master Log" not in sheet_name or "Combined Log" not in sheet_name else ((((len(headers)-col_num)-len(homework_session_types_headers))-1) if not is_autosave else (((len(headers)-col_num)-len(homework_session_types_headers))))
                            for typeh in entry.get("homework_details"):
                                #print(typeh)
                                for hwtype in self.all_homework_session_types:
                                    h_id = hwtype.get("id")
                                    name = hwtype.get("name")
                                    if typeh == h_id:
                                        found_status_for_mark_type2 = entry.get("homework_details").get(typeh).capitalize()
                                i += 1
                                ws.cell(row=row_num, column=col_num, value=found_status_for_mark_type2).alignment = right_alignment; col_num+=1
                        elif live_session_mode == "homework_session_s":
                            selected_options = session_details.get("selected_options", [])
                            
                            
                            s_correct = str(selected_options).removeprefix("[").removesuffix("]")
                            #s_total = len(selected_options)
                            #ws.cell(row=row_num, column=col_num, value=s_total).alignment = right_alignment; col_num+=1
                            ws.cell(row=row_num, column=col_num, value=s_correct).alignment = right_alignment; col_num+=1
                            """for hmt in self.settings.get("homework_mark_types", []): # Fill placeholders based on selected options
                                val_to_put = ""
                                if hmt["name"] in selected_options: # If a mark type name matches a selected option
                                    val_to_put = "Selected" # or hmt["default_points"]
                                    if "default_points" in hmt: total_hw_points += hmt["default_points"]
                                ws.cell(row=row_num, column=col_num, value=val_to_put).alignment = right_alignment; col_num+=1"""
                                
                                
                        else: # Unknown live mode or no details
                            for _ in self.settings.get("homework_mark_types", []): ws.cell(row=row_num, column=col_num, value="").alignment = right_alignment; col_num+=1

                    ws.cell(row=row_num, column=col_num, value=total_hw_points if total_hw_points else "").alignment = right_alignment; col_num+=1 # Total Points
                    ws.cell(row=row_num, column=col_num, value=effort_score_val).alignment = right_alignment; col_num+=1 # Effort

                comment_col = headers.index("Comment") + 1
                ws.cell(row=row_num, column=comment_col, value=entry.get("comment", "")).alignment = left_alignment
                if not separate_sheets or sheet_name == "Master Log":
                    log_type_col = headers.index("Log Type") + 1
                    ws.cell(row=row_num, column=log_type_col, value=entry.get("type", "behavior").capitalize())
                row_num += 1

            # Auto-size columns
            for col_letter in [get_column_letter(i) for i in range(1, ws.max_column + 1)]:
                max_length = 0
                column_values = [cell.value for cell in ws[col_letter]]
                for cell_val in column_values:
                    if cell_val is not None:
                        try: max_length = max(max_length, len(str(cell_val)))
                        except: pass
                adjusted_width = (max_length + 2) * 1.2
                ws.column_dimensions[col_letter].width = min(max(adjusted_width, 10), 50) # Min/Max width

        log_data_to_export = filtered_log

        # --- Individual Student Log Sheets ---
        if export_all_students_info: # Only create these if full export
            student_worksheets = {} # {student_id: worksheet_object}
            student_headers = ["Timestamp", "Type", "Behavior/Homework/Quiz Name",
                               "Correct/Did", "Total Qs/Total Selected", "Percentage", "Comment", "Day"]
            student_headers.extend(quiz_mark_type_headers) # Add mark types here too
            student_headers.extend(homework_mark_type_headers)
            student_headers.extend(homework_session_types_headers)
            # Put this line below back if it puts something in those columns
            #student_headers.extend(["Homework Type/Session Name", "Num Items"])

            for entry in log_data_to_export:
                student_id = entry["student_id"]
                student_data = self.students.get(student_id)
                student_name_for_sheet = self._make_safe_sheet_name(
                    f"{student_data['first_name']}_{student_data['last_name']}" if student_data else f"Unknown_{student_id}",
                    student_id
                )
                s_homework_marks_data = [""] * len(homework_mark_type_headers)
                
                if entry.get("type") == "homework" and "marks_data" in entry: # Graded manual log
                    total_hw_points = 0; effort_score_val = "" # For summary columns
                    hw_marks_data = entry.get("marks_data", {})
                    i=0
                    for hmt in self.settings.get("homework_mark_types", []):
                        val = hw_marks_data.get(hmt["id"], "")
                        #ws.cell(row=row_num, column=col_num, value=val).alignment = right_alignment; col_num+=1
                        if isinstance(val, (int,float)): total_hw_points += val # Sum points if numeric
                        if hmt["id"] == "hmark_effort": effort_score_val = val # Capture effort score
                        #for i, mt_config_h in enumerate(mark_type_configs_h):
                        s_homework_marks_data[i] = hw_marks_data.get(hmt["id"], "")
                        i+=1
                    #print(s_homework_marks_data)
                    
                    # Add headers for each homework mark type
                    #for hmt in self.settings.get("homework_mark_types", []): student_headers.append(hmt["name"])
                    
                    
                if student_id not in student_worksheets:
                    ws_student = wb.create_sheet(title=student_name_for_sheet)
                    student_worksheets[student_id] = ws_student
                    ws_student.append(student_headers)
                    for col_num, header_text in enumerate(student_headers, 1):
                        cell = ws_student.cell(row=1, column=col_num)
                        cell.font = OpenpyxlFont(bold=True)
                        cell.alignment = OpenpyxlAlignment(horizontal="center")
                        width = len(header_text) + 5 # Basic width
                        if header_text == "Timestamp": width = 20
                        elif header_text == "Behavior/Homework/Quiz Name": width = 30
                        elif header_text == "Type": width = 20
                        elif header_text == "Comment": width = 40
                        elif header_text == "Day": width = 12
                        ws_student.column_dimensions[get_column_letter(col_num)].width = width

                ws_student = student_worksheets[student_id]
                ts_obj_s = datetime.fromisoformat(entry["timestamp"])
                s_correct, s_total, s_perc = "", "", ""
                s_quiz_marks_data = [""] * len(quiz_mark_type_headers)
                all_h_types = self.all_homework_session_types
                s_homework_marks_data_2 = [""] * len(all_h_types)
                #print(all_h_types)
                
                
                if entry.get("type") == "quiz":
                    s_marks_data = entry.get("marks_data")
                    s_num_q = entry.get("num_questions", self.settings.get("default_quiz_questions",10))
                    if "score_details" in entry: # Live quiz
                        s_correct = entry["score_details"].get("correct", "")
                        s_total = entry["score_details"].get("total_asked", "")
                        if isinstance(s_correct, (int, float)) and isinstance(s_total, (int, float)) and s_total > 0:
                            s_perc = f"{round((s_correct / s_total) * 100)}%"
                    elif isinstance(s_marks_data, dict):
                        primary_correct_id_s = next((mt["id"] for mt in mark_type_configs if mt["name"].lower() == "correct"), "mark1")
                        s_correct = s_marks_data.get(primary_correct_id_s, "")
                        s_total = s_num_q
                        if isinstance(s_correct, (int,float)) and isinstance(s_total, (int,float)) and s_total > 0:
                            s_perc = f"{round((s_correct / s_total) * 100)}%"
                        for i, mt_config_s in enumerate(mark_type_configs):
                            s_quiz_marks_data[i] = s_marks_data.get(mt_config_s["id"], "")
                elif entry.get("type") == "homework_session_s":
                    s_correct = str(entry["homework_details"].get("selected_options")).removeprefix("[").removesuffix("]")
                    s_total = len(entry["homework_details"]["selected_options"])
                    #pass #print("Homework_session")
                elif entry.get("type") == "homework_session_y":
                    i = 0
                    #print(self.all_homework_session_types)
                    for typeh in entry.get("homework_details"):
                        #print(typeh)
                        for hwtype in all_h_types:
                            h_id = hwtype.get("id")
                            name = hwtype.get("name")
                            if typeh == h_id:
                                s_homework_marks_data_2[i] = entry.get("homework_details").get(typeh).capitalize()
                        i += 1
                            
                s_row_base = [
                    ts_obj_s.strftime('%Y-%m-%d %H:%M:%S'),
                    entry.get("type", "behavior").capitalize(),
                    entry.get("behavior", ""),
                    #entry.get("homework", ""),
                    s_correct, s_total, s_perc,
                    entry.get("comment", "").replace("\n", " "),
                    entry.get("day", "")
                ]
                s_row_base.extend(s_quiz_marks_data)
                s_row_base.extend(s_homework_marks_data)
                s_row_base.extend(s_homework_marks_data_2)
                
                ws_student.append(s_row_base)

        # --- Student Information Sheet ---
        #print((filtered_stud_ids))
        if export_all_students_info and len(filtered_stud_ids) > 1:
            students_info_ws = wb.create_sheet(title="Students Info")
            student_info_headers = ["Student ID", "First Name", "Last Name", "Nickname", "Full Name", "Gender", "Group Name"]
            students_info_ws.append(student_info_headers)
            for col_num, header in enumerate(student_info_headers, 1):
                cell = students_info_ws.cell(row=1, column=col_num)
                cell.font = OpenpyxlFont(bold=True); cell.alignment = OpenpyxlAlignment(horizontal="center")
                info_widths = {"Student ID": 15, "First Name": 15, "Last Name": 15, "Nickname": 15,
                               "Full Name": 25, "Gender": 10, "Group Name": 20}
                students_info_ws.column_dimensions[get_column_letter(col_num)].width = info_widths.get(header, 12)
            
            sorted_students_info = sorted(self.students.values(), key=lambda s: (s.get("last_name", "").lower(), s.get("first_name", "").lower()))

            for student_data in sorted_students_info:
                if student_data.get("id", "") in filtered_stud_ids:
                    group_id = student_data.get("group_id")
                    group_name = ""
                    if self.settings.get("student_groups_enabled", True) and group_id and group_id in self.student_groups:
                        group_name = self.student_groups[group_id].get("name", "")

                    info_row = [
                        student_data.get("id", ""), student_data.get("first_name", ""),
                        student_data.get("last_name", ""), student_data.get("nickname", ""),
                        student_data.get("full_name", ""), student_data.get("gender", ""), group_name
                    ]
                    students_info_ws.append(info_row)


        # Add Summary Sheet if requested
        if filter_settings.get("include_summaries", True) and filtered_log:
            ws_summary = wb.create_sheet(title="Summary")
            current_row = 1
            ws_summary.cell(row=current_row, column=1, value="Log Summary").font = OpenpyxlFont(bold=True, size=14); current_row += 2

            # Behavior Summary
            if filter_settings.get("include_behavior_logs", True):
                ws_summary.cell(row=current_row, column=1, value="Behavior Summary by Student").font = bold_font; current_row += 1
                b_headers = ["Student", "Behavior", "Count"]
                for c_num, h_title in enumerate(b_headers, 1): ws_summary.cell(row=current_row, column=c_num, value=h_title).font = OpenpyxlFont(italic=True)
                current_row += 1
                behavior_counts = {} # {student_id: {behavior_name: count}}
                for entry in filtered_log:
                    if entry.get("type") == "behavior":
                        sid = entry["student_id"]; b_name = entry.get("behavior")
                        behavior_counts.setdefault(sid, {}).setdefault(b_name, 0)
                        behavior_counts[sid][b_name] += 1
                for sid in sorted(behavior_counts.keys(), key=lambda x: student_data_for_export.get(x, {}).get("last_name","")):
                    s_info = student_data_for_export.get(sid, {"full_name": "Unknown"})
                    for b_name, count in sorted(behavior_counts[sid].items()):
                        ws_summary.cell(row=current_row, column=1, value=s_info["full_name"])
                        ws_summary.cell(row=current_row, column=2, value=b_name)
                        ws_summary.cell(row=current_row, column=3, value=count).alignment = right_alignment
                        current_row +=1
                current_row +=1 # Spacer

            # Quiz Summary
            if filter_settings.get("include_quiz_logs", True):
                ws_summary.cell(row=current_row, column=1, value="Quiz Averages by Student").font = bold_font; current_row += 1
                q_headers = ["Student", "Quiz Name", "Avg Score (%)", "Times Taken"]
                for c_num, h_title in enumerate(q_headers, 1): ws_summary.cell(row=current_row, column=c_num, value=h_title).font = OpenpyxlFont(italic=True)
                current_row += 1
                quiz_scores_summary = {} # {student_id: {quiz_name: [scores]}}
                for entry in filtered_log:
                    if entry.get("type") == "quiz":
                        sid = entry["student_id"]; q_name = entry.get("behavior"); num_q_s = entry.get("num_questions",0)
                        marks_d = entry.get("marks_data", {})
                        total_earned_s = 0; extra_credit_s = 0
                        for mt_s in self.settings.get("quiz_mark_types", []):
                            pts_s = marks_d.get(mt_s["id"], 0)
                            if pts_s > 0:
                                if mt_s.get("is_extra_credit", False): extra_credit_s += pts_s * mt_s.get("default_points",1)
                                else: total_earned_s += pts_s * mt_s.get("default_points",1)
                        main_q_total_possible_s = 0
                        correct_type_s = next((m for m in self.settings.get("quiz_mark_types",[]) if m.get("id") == "mark_correct"), None)
                        if correct_type_s and num_q_s > 0: main_q_total_possible_s = correct_type_s.get("default_points", 1) * num_q_s
                        score_val = ((total_earned_s + extra_credit_s) / main_q_total_possible_s) * 100 if main_q_total_possible_s > 0 else (100 if total_earned_s + extra_credit_s > 0 else 0)
                        quiz_scores_summary.setdefault(sid, {}).setdefault(q_name, []).append(score_val)
                for sid in sorted(quiz_scores_summary.keys(), key=lambda x: student_data_for_export.get(x, {}).get("last_name","")):
                    s_info = student_data_for_export.get(sid, {"full_name": "Unknown"})
                    for q_name, scores_list in sorted(quiz_scores_summary[sid].items()):
                        avg_score = sum(scores_list) / len(scores_list) if scores_list else 0
                        ws_summary.cell(row=current_row, column=1, value=s_info["full_name"])
                        ws_summary.cell(row=current_row, column=2, value=q_name)
                        ws_summary.cell(row=current_row, column=3, value=f"{avg_score:.2f}%").alignment = right_alignment
                        ws_summary.cell(row=current_row, column=4, value=len(scores_list)).alignment = right_alignment
                        current_row+=1
                current_row +=1

            # Homework Summary (New)
            if filter_settings.get("include_homework_logs", True):
                ws_summary.cell(row=current_row, column=1, value="Homework Completion by Student").font = bold_font; current_row += 1
                hw_headers = ["Student", "Homework Type/Session", "Count", "Total Points (if applicable)"]
                for c_num, h_title in enumerate(hw_headers, 1): ws_summary.cell(row=current_row, column=c_num, value=h_title).font = OpenpyxlFont(italic=True)
                current_row += 1
                homework_summary = {} # {student_id: {hw_type: {"count": 0, "total_points": 0}}}
                for entry in filtered_log:
                    if entry.get("type") == "homework" or entry.get("type") == "homework_session_s" or entry.get("type") == "homework_session_y":
                        sid = entry["student_id"]
                        hw_name = entry.get("homework_type", entry.get("behavior"))
                        summary_entry = homework_summary.setdefault(sid, {}).setdefault(hw_name, {"count": 0, "total_points": 0.0})
                        summary_entry["count"] += 1
                        # Sum points from marks_data for "homework" type
                        if entry.get("type") == "homework" and "marks_data" in entry:
                            for mark_id, mark_val in entry["marks_data"].items():
                                if isinstance(mark_val, (int, float)): summary_entry["total_points"] += mark_val
                        # Sum points from live session details (approximate)
                        elif entry.get("type") == "homework_session_y" or entry.get("type") == "homework_session_s":
                            hw_details = entry.get("homework_details", {})
                            live_mode = entry.get("type")
                            if live_mode == "homework_session_y":
                                for ht_id_key, status_val in hw_details.items():
                                     if status_val.lower() == "yes": # Simplified: 'yes' adds default points of 'complete' mark type
                                        complete_mark_type = next((m for m in self.settings.get("homework_mark_types",[]) if m["id"] == "hmark_complete"), None)
                                        if complete_mark_type: summary_entry["total_points"] += complete_mark_type.get("default_points",0)
                            elif live_mode == "homework_session_s":
                                selected_opts = hw_details.get("selected_options", [])
                                for opt_name in selected_opts:
                                    opt_mark_type = next((m for m in self.settings.get("homework_mark_types",[]) if m["name"] == opt_name), None)
                                    if opt_mark_type: summary_entry["total_points"] += opt_mark_type.get("default_points",0)

                for sid in sorted(homework_summary.keys(), key=lambda x: student_data_for_export.get(x, {}).get("last_name","")):
                    s_info = student_data_for_export.get(sid, {"full_name": "Unknown"})
                    for hw_name, data in sorted(homework_summary[sid].items()):
                        ws_summary.cell(row=current_row, column=1, value=s_info["full_name"])
                        ws_summary.cell(row=current_row, column=2, value=hw_name)
                        ws_summary.cell(row=current_row, column=3, value=data["count"]).alignment = right_alignment
                        ws_summary.cell(row=current_row, column=4, value=f"{data['total_points']:.2f}" if data['total_points'] else "").alignment = right_alignment
                        current_row += 1
                current_row += 1

            for col_letter_s in [get_column_letter(i) for i in range(1, ws_summary.max_column + 1)]:
                 ws_summary.column_dimensions[col_letter_s].width = 25


        # Save workbook
        try:
            wb.save(filename=file_path)
        except PermissionError as e:
            if is_autosave:
                print(f"Autosave PermissionError: {e}. File might be open.")
                # Don't show messagebox for autosave, just print
            else:
                messagebox.showerror("Save Error", f"Permission denied. Could not save to '{file_path}'.\nPlease ensure the file is not open in another program and you have write permissions.", parent=self.root)
            raise # Re-raise to be caught by the calling function for status update
        except Exception as e_save:
            if is_autosave: print(f"Autosave error: {e_save}")
            else: messagebox.showerror("Save Error", f"An unexpected error occurred while saving Excel file: {e_save}", parent=self.root)
            raise

    def export_data_to_csv_zip(self, zip_file_path, filter_settings=None):
        # ... (updated for new log types and filtering)
        temp_dir = tempfile.mkdtemp()
        try:
            student_data_for_export = {sid: {"first_name": s["first_name"], "last_name": s["last_name"], "full_name": s["full_name"]} for sid, s in self.students.items()}
            logs_to_process_csv = []
            if filter_settings.get("include_behavior_logs", True): logs_to_process_csv.extend([log for log in self.behavior_log if log.get("type") == "behavior"])
            if filter_settings.get("include_quiz_logs", True): logs_to_process_csv.extend([log for log in self.behavior_log if log.get("type") == "quiz"])
            if filter_settings.get("include_homework_logs", True): logs_to_process_csv.extend([log for log in self.homework_log if log.get("type") == "homework" or log.get("type") == "homework_session_y" or log.get("type") == "homework_session_s"])

            filtered_log_csv = []
            start_date_csv, end_date_csv = filter_settings.get("start_date"), filter_settings.get("end_date")
            sel_students_opt_csv, student_ids_flt_csv = filter_settings.get("selected_students", "all"), filter_settings.get("student_ids", [])
            sel_behaviors_opt_csv, behaviors_flt_csv = filter_settings.get("selected_behaviors", "all"), filter_settings.get("behaviors_list", [])
            sel_hw_opt_csv, hw_flt_csv = filter_settings.get("selected_homework_types", "all"), filter_settings.get("homework_types_list", [])

            for entry in logs_to_process_csv:
                try:
                    entry_date = datetime.fromisoformat(entry["timestamp"]).date()
                    if start_date_csv and entry_date < start_date_csv: continue
                    if end_date_csv and entry_date > end_date_csv: continue
                except ValueError: continue
                if sel_students_opt_csv == "specific" and entry["student_id"] not in student_ids_flt_csv: continue
                log_type_csv = entry.get("type", "behavior")
                entry_name_csv = entry.get("behavior")
                if log_type_csv == "homework" or log_type_csv == "homework_session": entry_name_csv = entry.get("homework_type", entry.get("behavior"))

                if log_type_csv == "behavior" or log_type_csv == "quiz":
                    if sel_behaviors_opt_csv == "specific" and entry_name_csv not in behaviors_flt_csv: continue
                elif log_type_csv == "homework" or log_type_csv == "homework_session":
                    if sel_hw_opt_csv == "specific" and entry_name_csv not in hw_flt_csv: continue
                filtered_log_csv.append(entry)
            filtered_log_csv.sort(key=lambda x: x["timestamp"])

            # CSV file for all logs (or separate if preferred, but Excel handles separation better)
            all_logs_csv_path = os.path.join(temp_dir, "all_logs.csv")
            with open(all_logs_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = ["Timestamp", "Date", "Time", "Day", "Student_ID", "First_Name", "Last_Name",
                              "Log_Type", "Item_Name", "Comment", "Num_Questions_Items",
                              "Marks_Data_JSON", "Score_Details_JSON", "Homework_Details_JSON"]
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames, extrasaction='ignore')
                writer.writeheader()
                for entry in filtered_log_csv:
                    student_info = student_data_for_export.get(entry["student_id"], {"first_name": "N/A", "last_name": "N/A"})
                    try: dt_obj = datetime.fromisoformat(entry["timestamp"])
                    except ValueError: dt_obj = datetime.now()
                    row_data = {
                        "Timestamp": entry["timestamp"], "Date": dt_obj.strftime('%Y-%m-%d'), "Time": dt_obj.strftime('%H:%M:%S'),
                        "Day": entry.get("day", dt_obj.strftime('%A')), "Student_ID": entry["student_id"],
                        "First_Name": student_info["first_name"], "Last_Name": student_info["last_name"],
                        "Log_Type": entry.get("type", "").capitalize(),
                        "Item_Name": entry.get("behavior", entry.get("homework_type", "")),
                        "Comment": entry.get("comment", ""),
                        "Num_Questions_Items": entry.get("num_questions", entry.get("num_items")),
                        "Marks_Data_JSON": json.dumps(entry.get("marks_data")) if "marks_data" in entry else "",
                        "Score_Details_JSON": json.dumps(entry.get("score_details")) if "score_details" in entry else "",
                        "Homework_Details_JSON": json.dumps(entry.get("homework_details")) if "homework_details" in entry else ""
                    }
                    writer.writerow(row_data)

            # CSV file for student list
            students_csv_path = os.path.join(temp_dir, "students.csv")
            with open(students_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames_s = ["Student_ID", "First_Name", "Last_Name", "Nickname", "Gender", "Group_ID"]
                writer_s = csv.DictWriter(csvfile, fieldnames=fieldnames_s, extrasaction='ignore')
                writer_s.writeheader()
                for sid, sdata in self.students.items():
                     writer_s.writerow({"Student_ID": sid, "First_Name": sdata["first_name"], "Last_Name": sdata["last_name"],
                                        "Nickname": sdata.get("nickname",""), "Gender": sdata.get("gender",""), "Group_ID": sdata.get("group_id","")})

            # Create ZIP file
            with zipfile.ZipFile(zip_file_path, 'w', zipfile.ZIP_DEFLATED) as zf:
                zf.write(all_logs_csv_path, arcname="all_logs.csv")
                zf.write(students_csv_path, arcname="students.csv")
                if filter_settings.get("include_summaries", False): # type: ignore # Basic summary text file
                    summary_txt_path = os.path.join(temp_dir, "summary.txt")
                    with open(summary_txt_path, 'w', encoding='utf-8')as f_sum:
                        f_sum.write(f"Log Export Summary - {datetime.now().strftime('%Y-%m-%d %H:%M')}\n")
                        f_sum.write(f"Date Range: {start_date_csv or 'Any'} to {end_date_csv or 'Any'}\n")
                        f_sum.write(f"Total Log Entries Exported: {len(filtered_log_csv)}\n")
                        # Further summary details could be added here
                    zf.write(summary_txt_path, arcname="summary.txt")

        finally: shutil.rmtree(temp_dir) # Clean up temp directory

    def export_layout_as_image(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Export Image", "Enter password to export layout as image:"): return
        file_path = filedialog.asksaveasfilename(defaultextension=".png", initialfile=f"layout_export_{datetime.now().strftime('%Y%m%d_%H%M%S')}.png",
                                               filetypes=[("PNG Image", "*.png"), ("JPG Image", "*.jpg"), ("WebP Image", "*.webp"), ("TIFF Image", "*.tiff"), ("BMP Image", "*.bmp"), ("PPM Image", "*.ppm"), ("PGM Image", "*.pgm"),("GIF Image", "*.gif"), ("All files", "*.*")], parent=self.root)
        if not file_path: self.update_status("Image export cancelled."); return
        try:
            # Determine current bounds of drawn items on canvas (in canvas coordinates)
            # This uses the scrollregion which should be set by draw_all_items
            s_region = self.canvas.cget("scrollregion")
            if not s_region: # Fallback if scrollregion is not set (e.g. empty canvas)
                 x1, y1, x2, y2 = 0,0, self.canvas.winfo_width(), self.canvas.winfo_height()
            else:
                try: 
                    v= s_region.split()
                    x1 = float(v[0])
                    y1 = float(v[1])
                    x2 = float(v[2])
                    y2 = float(v[3])
                except Exception as e: x1, y1, x2, y2 = 0,0, self.canvas.winfo_width(), self.canvas.winfo_height()
            
            # Ensure x1, y1 are not negative for postscript (though typically they are 0 or positive)
            # If they are negative, it means content is scrolled left/up off screen.
            # We want to capture from the top-leftmost content.
            
            postscript_x_offset = -x1 if x1 < 0 else 0
            postscript_y_offset = -y1 if y1 < 0 else 0
            
            # Create PostScript of the entire scrollable region
            ps_io = io.BytesIO()
            timestamp = str(IMAGENAMEW)
            self.canvas.postscript( # type: ignore
                x=x1 + postscript_x_offset,
                y=y1 + postscript_y_offset,
                width=x2 - x1, # Width of the scrollable area
                height=y2 - y1, # Height of the scrollable area
                colormode='color',
                file=(timestamp) # Write to BytesIO object
            )
            ps_io.seek(0)
            
            output_dpi = int(self.settings.get("output_dpi", 600))
            
            try:
                img = Image.open(os.path.abspath(timestamp))
                ps_file = ps_io
                output_image_file = file_path
                #output_dpi = 600 
                scale_factor = output_dpi / 72.0
                try: img.load(scale=scale_factor)  # type: ignore
                except AttributeError:
                    print("Warning: img.load(scale=...) might not be directly supported for .ps files in your Pillow version in this way.")
                    print("Pillow will use Ghostscript's default rasterization or a pre-set one.")
                    # If direct scaling isn't working, you might need to use subprocess for full control (see advanced section).

                # Now save the image. The 'dpi' parameter here is metadata for formats like PNG/TIFF.
                # The actual pixel dimensions are determined by the rasterization step.
                img.save(output_image_file, dpi=(output_dpi, output_dpi))
                print(f"PostScript file '{timestamp}' converted to '{output_image_file}' at {output_dpi} DPI.")     
                
                
                img.save(file_path, "png")
                self.update_status(f"Layout exported as image: {os.path.basename(file_path)}")
                if messagebox.askyesno("Export Successful", f"Layout image saved to:\n{file_path}\n\nDo you want to open the file location?", parent=self.root):
                    self.open_specific_export_folder(file_path)
            except (OSError, PIL.UnidentifiedImageError) as e_pil:
                print(f"PIL error processing PostScript: {e_pil}")
                if "gs" in str(e_pil).lower() or "ghostscript" in str(e_pil).lower():
                     messagebox.showerror("Image Export Error", "Failed to convert PostScript to image. Ghostscript might not be installed or found in your system's PATH. Please install Ghostscript to enable image export.", parent=self.root)
                else:
                     messagebox.showerror("Image Export Error", f"Failed to save image: {e_pil}.\nEnsure you have image processing libraries like Pillow and its dependencies (e.g., Ghostscript for EPS/PS) installed.", parent=self.root)
            except Exception as e: print("e2", e)
            finally:
                ps_io.close()
                """try: img.close(); os.remove(os.path.abspath(IMAGENAMEW))
                except FileNotFoundError: pass
                except: pass"""

        except tk.TclError as e_tk:
            messagebox.showerror("Image Export Error", f"Tkinter error during PostScript generation: {e_tk}", parent=self.root)
        except Exception as e:
            messagebox.showerror("Image Export Error", f"An unexpected error occurred: {e}", parent=self.root); print("e", e)
        finally:
            self.password_manager.record_activity()
        
    def _import_data_from_excel_logic(self, file_path, import_incidents_flag, student_sheet_name_to_import):
        # This function needs significant updates if we want to import detailed quiz scores.
        # For now, it will import students and basic incident info as before.
        # Importing complex quiz scores from Excel would require a well-defined column mapping.
        workbook = load_workbook(filename=file_path, data_only=True)
        imported_student_count = 0

        # --- Import Students ---
        if student_sheet_name_to_import:
            if student_sheet_name_to_import not in workbook.sheetnames:
                messagebox.showerror("Import Error", f"Selected student sheet '{student_sheet_name_to_import}' not found.", parent=self.root)
                return 0, 0

            sheet = workbook[student_sheet_name_to_import]
            header_row_values = [str(cell.value).lower().strip() if cell.value else "" for cell in sheet[1]]

            # Try to find columns for student data
            col_indices = {}
            common_headers = {
                "first_name": ["first", "first name", "firstname"],
                "last_name": ["last", "last name", "lastname", "surname"],
                "full_name": ["full name", "name", "student name"],
                "nickname": ["nickname", "preferred name", "nick"],
                "gender": ["gender", "sex"],
                "group_name": ["group", "group name", "student group"] # For importing group assignment by name
            }
            for key, common_list in common_headers.items():
                for idx, header_val in enumerate(header_row_values):
                    if header_val in common_list:
                        col_indices[key] = idx; break

            # Fallback for full name if "name" exists
            if "name" in header_row_values and "full_name" not in col_indices:
                try: col_indices["full_name"] = header_row_values.index("name")
                except ValueError: pass

            # Basic name column check
            if "first_name" not in col_indices or "last_name" not in col_indices:
                if "full_name" not in col_indices:
                    if messagebox.askyesno("Column Ambiguity", f"Could not auto-detect specific name columns in '{student_sheet_name_to_import}'.\n"
                                           "Assume Col A = First, Col B = Last? \n(No assumes Col A = 'Last, First' or 'First Last')", parent=self.root):
                        col_indices["first_name"], col_indices["last_name"] = 0, 1
                    else: col_indices["full_name"] = 0


            existing_full_names_in_app = {s['full_name'].lower().strip(): s['id'] for s in self.students.values()}
            commands_to_add_students = []
            current_id_num_for_batch = self.next_student_id_num # Use app's current next ID

            for row_idx, row_values_tuple in enumerate(sheet.iter_rows(min_row=2, values_only=True)):
                row_values = list(row_values_tuple)
                first_name, last_name, nickname, gender, group_id_to_assign = None, None, "", "Boy", None

                def get_val(key_idx, default=""):
                    idx = col_indices.get(key_idx)
                    if idx is not None and idx < len(row_values) and row_values[idx] is not None:
                        return str(row_values[idx]).strip()
                    return default

                first_name = get_val("first_name", None)
                last_name = get_val("last_name", None)
                nickname = get_val("nickname", "")
                gender_str = get_val("gender", "Boy").lower()
                if gender_str in ["girl", "female", "f"]: gender = "Girl"

                group_name_from_excel = get_val("group_name", "")
                if group_name_from_excel and self.settings.get("student_groups_enabled", True):
                    # Find group_id by name
                    for gid, gdata in self.student_groups.items():
                        if gdata.get("name", "").lower() == group_name_from_excel.lower():
                            group_id_to_assign = gid; break


                if not first_name or not last_name: # Try parsing from full_name if specific cols missing
                    if "full_name" in col_indices:
                        full_name_str = get_val("full_name", None)
                        if full_name_str:
                            if "," in full_name_str:
                                parts = full_name_str.split(",", 1)
                                last_name = parts[0].strip()
                                first_name = parts[1].strip() if len(parts) > 1 else ""
                            elif " " in full_name_str: # Assume "First Last"
                                parts = full_name_str.split(" ", 1)
                                first_name = parts[0].strip()
                                last_name = parts[1].strip() if len(parts) > 1 else ""
                            else: first_name = full_name_str # Single word as first name

                if first_name: # Must have at least a first name
                    if not last_name: last_name = "" # Ensure last_name is a string

                    full_name_display = f"{first_name} \"{nickname}\" {last_name}" if nickname else f"{first_name} {last_name}"
                    full_name_key = full_name_display.lower().strip()

                    if full_name_key not in existing_full_names_in_app:
                        student_id_str = f"student_{current_id_num_for_batch}"
                        old_next_id_for_command = current_id_num_for_batch # For AddItemCommand's undo logic
                        next_id_for_app_after_this = current_id_num_for_batch + 1

                        x_pos = 50 + (imported_student_count % 10) * (self.settings.get("default_student_box_width", DEFAULT_STUDENT_BOX_WIDTH) + 10)
                        y_pos = 50 + (imported_student_count // 10) * (self.settings.get("default_student_box_height", DEFAULT_STUDENT_BOX_HEIGHT) + 10)

                        s_data = {
                            "first_name": first_name, "last_name": last_name, "nickname": nickname,
                            "gender": gender, "full_name": full_name_display,
                            "x": x_pos, "y": y_pos, "id": student_id_str,
                            "width": self.settings.get("default_student_box_width"),
                            "height": self.settings.get("default_student_box_height"),
                            "original_next_id_num_after_add": next_id_for_app_after_this,
                            "group_id": group_id_to_assign,
                            "style_overrides": {}
                        }
                        cmd = AddItemCommand(self, student_id_str, 'student', s_data, old_next_id_for_command)
                        commands_to_add_students.append(cmd)
                        existing_full_names_in_app[full_name_key] = student_id_str # Add to check for this batch
                        imported_student_count += 1
                        current_id_num_for_batch += 1
                    else: # Student exists, maybe update their group?
                        existing_student_id = existing_full_names_in_app[full_name_key]
                        if group_id_to_assign and self.students[existing_student_id].get("group_id") != group_id_to_assign:
                            # Create an EditItemCommand to update group_id
                            old_data_snapshot = self.students[existing_student_id].copy()
                            changes = {"group_id": group_id_to_assign}
                            # No need for AddItemCommand here, it's an edit.
                            # This part could be more complex if we want to batch these edits.
                            # For now, let's assume import primarily adds new students.
                            print(f"Student {full_name_display} already exists. Group update from Excel not yet fully implemented here.")


            for cmd in commands_to_add_students:
                self.execute_command(cmd) # This will update self.next_student_id_num via AddItemCommand

            if commands_to_add_students: # If any students were added
                 self.next_student_id_num = current_id_num_for_batch # Ensure app's counter is past the last used ID

        # --- Import Incidents (Simplified - does not import detailed quiz marks yet) ---
        imported_incident_count = 0
        if import_incidents_flag:
            incident_commands_to_add = []
            for sheet_name_excel in workbook.sheetnames:
                # Try to match Excel sheet name (e.g., "FirstName_LastName") to an existing student
                matched_student_id, matched_student_first_name, matched_student_last_name = None, "", ""
                normalized_excel_sheet_name_for_match = sheet_name_excel.replace("_", " ").lower()

                for s_id_app, s_data_app in self.students.items():
                    # Check against "FirstName LastName" and "FirstName_LastName" formats
                    app_student_full_name_match = s_data_app['full_name'].lower()
                    app_student_export_format_match = f"{s_data_app['first_name']}_{s_data_app['last_name']}".lower()
                    if normalized_excel_sheet_name_for_match == app_student_full_name_match or \
                       sheet_name_excel.lower() == app_student_export_format_match:
                        matched_student_id = s_id_app
                        matched_student_first_name = s_data_app['first_name']
                        matched_student_last_name = s_data_app['last_name']
                        break

                if matched_student_id:
                    student_sheet_incidents = workbook[sheet_name_excel]
                    s_header_values = [str(cell.value).lower().strip() if cell.value else "" for cell in student_sheet_incidents[1]]
                    col_map_incidents = {}
                    try: # Basic incident columns
                        col_map_incidents["ts"] = s_header_values.index("timestamp")
                        col_map_incidents["type"] = s_header_values.index("type")
                        col_map_incidents["beh_quiz_name"] = s_header_values.index("behavior/quiz name")
                        # For quiz scores, we'd look for "Correct", "Total Qs" or specific mark type columns
                        # This part is simplified for now.
                        col_map_incidents["score_correct"] = s_header_values.index("correct") if "correct" in s_header_values else -1
                        col_map_incidents["score_total_qs"] = s_header_values.index("total qs") if "total qs" in s_header_values else -1
                        col_map_incidents["comment"] = s_header_values.index("comment")
                        col_map_incidents["day"] = s_header_values.index("day")
                    except ValueError:
                        print(f"Skipping sheet '{sheet_name_excel}' for incident import: missing expected basic headers.")
                        continue

                    for row_idx_inc, s_row_values_tuple_inc in enumerate(student_sheet_incidents.iter_rows(min_row=2, values_only=True)):
                        s_row_values_inc = list(s_row_values_tuple_inc)
                        try:
                            timestamp_str = str(s_row_values_inc[col_map_incidents["ts"]]) if col_map_incidents["ts"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["ts"]] else None
                            log_type_str = str(s_row_values_inc[col_map_incidents["type"]]).lower() if col_map_incidents["type"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["type"]] else "behavior"
                            behavior_quiz_name_str = str(s_row_values_inc[col_map_incidents["beh_quiz_name"]]) if col_map_incidents["beh_quiz_name"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["beh_quiz_name"]] else ""
                            comment_str = str(s_row_values_inc[col_map_incidents["comment"]]) if col_map_incidents["comment"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["comment"]] else ""
                            day_str = str(s_row_values_inc[col_map_incidents["day"]]) if col_map_incidents["day"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["day"]] else ""

                            if not timestamp_str or not behavior_quiz_name_str: continue

                            parsed_dt = None
                            if isinstance(s_row_values_inc[col_map_incidents["ts"]], datetime): parsed_dt = s_row_values_inc[col_map_incidents["ts"]]
                            else:
                                try: parsed_dt = datetime.strptime(timestamp_str, '%Y-%m-%d %H:%M:%S')
                                except ValueError:
                                    try: parsed_dt = datetime.fromisoformat(timestamp_str)
                                    except ValueError:
                                        print(f"Skipping incident in '{sheet_name_excel}', row {row_idx_inc+2}: bad timestamp format '{timestamp_str}'")
                                        continue
                            iso_timestamp = parsed_dt.isoformat()

                            # Check for duplicates before adding
                            is_duplicate = False
                            for existing_log in self.behavior_log:
                                if existing_log["student_id"] == matched_student_id and \
                                   existing_log["timestamp"] == iso_timestamp and \
                                   existing_log["behavior"] == behavior_quiz_name_str and \
                                   existing_log.get("type","behavior") == log_type_str:
                                    is_duplicate = True; break

                            if not is_duplicate:
                                log_entry_data = {
                                    "timestamp": iso_timestamp, "student_id": matched_student_id,
                                    "student_first_name": matched_student_first_name,
                                    "student_last_name": matched_student_last_name,
                                    "behavior": behavior_quiz_name_str, "comment": comment_str,
                                    "type": log_type_str, "day": day_str
                                }
                                # Simplified quiz score import (just basic correct/total if available)
                                if log_type_str == "quiz":
                                    correct_val_imp = None
                                    total_qs_val_imp = None
                                    if col_map_incidents["score_correct"] != -1 and col_map_incidents["score_correct"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["score_correct"]]:
                                        try: correct_val_imp = int(s_row_values_inc[col_map_incidents["score_correct"]])
                                        except ValueError: pass
                                    if col_map_incidents["score_total_qs"] != -1 and col_map_incidents["score_total_qs"] < len(s_row_values_inc) and s_row_values_inc[col_map_incidents["score_total_qs"]]:
                                        try: total_qs_val_imp = int(s_row_values_inc[col_map_incidents["score_total_qs"]])
                                        except ValueError: pass

                                    if correct_val_imp is not None and total_qs_val_imp is not None:
                                        # Store as simple score_details for now, similar to live quiz
                                        log_entry_data["score_details"] = {"correct": correct_val_imp, "total_asked": total_qs_val_imp}
                                        log_entry_data["num_questions"] = total_qs_val_imp # Assume total_asked is num_questions
                                    elif correct_val_imp is not None: # If only correct is found, log it as a raw score string
                                        log_entry_data["score"] = str(correct_val_imp)


                                cmd = LogEntryCommand(self, log_entry_data, matched_student_id, timestamp=iso_timestamp)
                                incident_commands_to_add.append(cmd)
                                imported_incident_count += 1
                        except IndexError:
                            print(f"Skipping row {row_idx_inc + 2} in '{sheet_name_excel}' for incident import: missing data columns.")
                            continue
            for cmd in incident_commands_to_add:
                self.execute_command(cmd)

        return imported_student_count, imported_incident_count

    def import_students_from_excel_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Import", "Enter password to import from Excel:"): return

        dialog = ImportExcelOptionsDialog(self.root, app_instance=self)
        if dialog.result:
            file_path, import_incidents_flag, student_sheet_name = dialog.result
            if not file_path: return
            try:
                imported_student_count, imported_incident_count = self._import_data_from_excel_logic(file_path, import_incidents_flag, student_sheet_name)
                status_msg = f"Imported {imported_student_count} new students"
                if import_incidents_flag: status_msg += f" and {imported_incident_count} new incidents"
                status_msg += ". Duplicates were skipped."
                self.update_status(status_msg)
                self.draw_all_items(check_collisions_on_redraw=True)
                self.save_data_wrapper(source="import_excel") # Save after successful import
                self.password_manager.record_activity()
            except Exception as e:
                messagebox.showerror("Import Error", f"Failed to import from Excel: {e}", parent=self.root)
                self.update_status(f"Error during Excel import: {e}")
                import traceback
                traceback.print_exc()

    def save_layout_template_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Save Layout", "Enter password to save layout template:"): return
        template_name = simpledialog.askstring("Save Layout Template", "Enter a name for this layout template:", parent=self.root)
        if template_name and template_name.strip():
            filename = "".join(c if c.isalnum() or c in (' ', '_', '-') else '_' for c in template_name.strip()) + ".json"
            file_path = os.path.join(LAYOUT_TEMPLATES_DIR, filename)
            layout_data = {
                "students": {
                    sid: {
                        "x": s["x"], "y": s["y"],
                        "width": s.get("width"), "height": s.get("height"),
                        "style_overrides": s.get("style_overrides",{}).copy(),
                        # Add name details for robust loading
                        "first_name": s.get("first_name", ""),
                        "last_name": s.get("last_name", ""),
                        "nickname": s.get("nickname", "")
                    } for sid, s in self.students.items()
                },
                "furniture": {
                    fid: {
                        "x": f["x"], "y": f["y"],
                        "width": f.get("width"), "height": f.get("height")
                    } for fid, f in self.furniture.items()
                }
            }
            try:
                self._encrypt_and_write_file(file_path, layout_data)
                self.update_status(f"Layout template '{template_name}' saved.")
            except Exception as e: messagebox.showerror("Save Error", f"Could not save layout template: {e}", parent=self.root)
        else: self.update_status("Layout template save cancelled.")
        self.password_manager.record_activity()

    def load_layout_template_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Load Layout", "Enter password to load layout template:"): return
        if not os.path.exists(LAYOUT_TEMPLATES_DIR) or not os.listdir(LAYOUT_TEMPLATES_DIR):
            messagebox.showinfo("No Templates", "No layout templates found in default folder.", parent=self.root); 
        file_path = filedialog.askopenfilename(initialdir=LAYOUT_TEMPLATES_DIR, title="Select Layout Template",
                                               filetypes=[("JSON files", "*.json"), ("All files", "*.*")], parent=self.root)
        if file_path:
            try:
                template_data = self._read_and_decrypt_file(file_path)
                if not isinstance(template_data, dict):
                    raise json.JSONDecodeError("Invalid template format.", "", 0)

                if messagebox.askyesno("Confirm Load", "Loading this template will overwrite current item positions and sizes. Student data (names, logs) will be preserved. Continue?", parent=self.root):
                    # ... (rest of the logic remains the same)
                    move_commands_data = []
                    size_commands_data = []
                    template_students = template_data.get("students", {})
                    template_furniture = template_data.get("furniture", {})

                    applied_count = 0
                    skipped_count = 0
                    name_match_log = []
                    match_by_name = messagebox.askyesno("Layout Loading Options", "Load layout template by names of students (doesn't need to be exact) or by ID (not preferred-doesn't preserve student positions correctly)?\nYes is by names, no is by ID.")
                    for template_student_id, t_stud_data in template_students.items():
                        target_student_id = None
                        s_current = None
                        if match_by_name:
                            # 2. Secondary Match: Name (first, last, then nickname for disambiguation)
                            t_first = t_stud_data.get("first_name", "").lower()
                            t_last = t_stud_data.get("last_name", "").lower()
                            t_nick = t_stud_data.get("nickname", "").lower()

                            if not t_first or not t_last: # Cannot match by name if essential parts are missing
                                name_match_log.append(f"Skipped template student (ID: {template_student_id}, Name: {t_stud_data.get('full_name', 'N/A')}) due to missing name components in template.")
                                skipped_count +=1
                                continue

                            potential_matches = []
                            for c_sid, c_sdata in self.students.items():
                                if c_sdata.get("first_name", "").lower() == t_first and \
                                   c_sdata.get("last_name", "").lower() == t_last:
                                    potential_matches.append(c_sid)

                            if len(potential_matches) == 1:
                                target_student_id = potential_matches[0]
                                s_current = self.students[target_student_id]
                                name_match_log.append(f"Matched template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} to classroom's {s_current['full_name']} by name.")
                            elif len(potential_matches) > 1:
                                # Attempt disambiguation with nickname
                                if t_nick:
                                    final_matches = [pid for pid in potential_matches if self.students[pid].get("nickname","").lower() == t_nick]
                                    if len(final_matches) == 1:
                                        target_student_id = final_matches[0]
                                        s_current = self.students[target_student_id]
                                        name_match_log.append(f"Matched template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} ({t_nick}) to classroom's {s_current['full_name']} by exact name & nickname.")
                                    else: # No exact nickname match, or multiple after filtering by nickname
                                        name_match_log.append(f"Ambiguous exact name match for template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} (Nickname: {t_nick}). Found {len(potential_matches)} with same first/last, {len(final_matches)} after nickname filter. Trying fuzzy match.")
                                        # Proceed to fuzzy matching for these potential_matches if final_matches was not unique
                                        potential_matches_for_fuzzy = final_matches if t_nick and final_matches else potential_matches
                                        # Fall through to fuzzy matching logic below if no unique exact match yet
                                else: # No nickname in template to disambiguate exact first/last name matches
                                    name_match_log.append(f"Ambiguous exact name match for template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')}. Found {len(potential_matches)} classroom students. Trying fuzzy match.")
                                    # Fall through to fuzzy matching logic below

                            # Fuzzy Matching Stage (if no unique exact match by ID or full name + nickname)
                            if not target_student_id: # Only if we haven't found a target yet
                                fuzzy_matches = []
                                # If potential_matches had some exact first/last name hits, fuzzy match within that subset first
                                students_to_search_fuzzy = [self.students[pid] for pid in potential_matches] if potential_matches else list(self.students.values())

                                for c_sdata_fuzzy in students_to_search_fuzzy:
                                    # Construct full names for comparison
                                    template_full_name_for_fuzzy = f"{t_first} {t_last}"
                                    classroom_full_name_for_fuzzy = f"{c_sdata_fuzzy.get('first_name','').lower()} {c_sdata_fuzzy.get('last_name','').lower()}"

                                    similarity = name_similarity_ratio(template_full_name_for_fuzzy, classroom_full_name_for_fuzzy)

                                    if similarity >= 0.85: # Similarity threshold
                                        fuzzy_matches.append({"id": c_sdata_fuzzy["id"], "similarity": similarity, "data": c_sdata_fuzzy})

                                if fuzzy_matches:
                                    fuzzy_matches.sort(key=lambda x: x["similarity"], reverse=True) # Sort by best match

                                    if len(fuzzy_matches) == 1 or fuzzy_matches[0]["similarity"] > fuzzy_matches[1]["similarity"] + 0.05: # Unique best fuzzy match or significantly better
                                        best_fuzzy_match = fuzzy_matches[0]
                                        target_student_id = best_fuzzy_match["id"]
                                        s_current = self.students[target_student_id]
                                        name_match_log.append(f"Fuzzy matched template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} to classroom's {s_current['full_name']} (Similarity: {best_fuzzy_match['similarity']:.2f}).")
                                    else: # Multiple good fuzzy matches, try nickname disambiguation again
                                        if t_nick:
                                            final_fuzzy_nick_matches = [fm for fm in fuzzy_matches if fm["data"].get("nickname","").lower() == t_nick and fm["similarity"] >=0.85]
                                            if len(final_fuzzy_nick_matches) == 1:
                                                target_student_id = final_fuzzy_nick_matches[0]["id"]
                                                s_current = self.students[target_student_id]
                                                name_match_log.append(f"Fuzzy matched (with nickname) template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} ({t_nick}) to classroom's {s_current['full_name']} (Similarity: {final_fuzzy_nick_matches[0]['similarity']:.2f}).")
                                            else:
                                                name_match_log.append(f"Ambiguous fuzzy match for template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} ({t_nick}) after nickname. Skipped.")
                                                skipped_count += 1
                                        else:
                                            name_match_log.append(f"Ambiguous fuzzy match for template's {t_stud_data.get('first_name')} {t_stud_data.get('last_name')}. Skipped.")
                                            skipped_count += 1
                                elif not potential_matches : # Only log "no match" if there were no exact first/last name potential_matches initially
                                    name_match_log.append(f"No ID, exact name, or close fuzzy match for template student {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} (ID: {template_student_id}). Skipped.")
                                    skipped_count += 1

                            # If after all matching attempts, still no target_student_id
                            if not target_student_id and not potential_matches : # Redundant check for skipped_count already done by fuzzy logic.
                                # This log might be duplicated if fuzzy also logged a skip.
                                # name_match_log.append(f"Final skip for template student {t_stud_data.get('first_name')} {t_stud_data.get('last_name')} (ID: {template_student_id}).")
                                # skipped_count +=1 # This might double count skips if fuzzy already counted it.
                                pass
                        else:   # 2. ID Match
                            if template_student_id in self.students:
                                target_student_id = template_student_id
                                s_current = self.students[target_student_id]

                        # If a student was found (either by ID, exact name, or fuzzy name)
                        if target_student_id and s_current:
                            applied_count +=1
                            # Position
                            old_x, old_y = s_current["x"], s_current["y"]
                            new_x, new_y = t_stud_data.get("x", old_x), t_stud_data.get("y", old_y)
                            if old_x != new_x or old_y != new_y:
                                move_commands_data.append({'id':target_student_id, 'type':'student', 'old_x':old_x, 'old_y':old_y, 'new_x':new_x, 'new_y':new_y})
                            
                            # Size
                            old_w = s_current.get("style_overrides",{}).get("width", s_current.get("width", DEFAULT_STUDENT_BOX_WIDTH))
                            old_h = s_current.get("style_overrides",{}).get("height", s_current.get("height", DEFAULT_STUDENT_BOX_HEIGHT))
                            new_w = t_stud_data.get("width", old_w)
                            new_h = t_stud_data.get("height", old_h)
                            if old_w != new_w or old_h != new_h:
                                size_commands_data.append({'id':target_student_id, 'type':'student', 'old_w':old_w, 'old_h':old_h, 'new_w':new_w, 'new_h':new_h})
                            
                            # Style Overrides
                            t_style_overrides = t_stud_data.get("style_overrides", {})
                            if t_style_overrides or (not t_style_overrides and s_current.get("style_overrides")): # Apply if template has styles OR if current has styles that need clearing
                                current_style_snapshot = s_current.get("style_overrides", {}).copy()

                                # Create a snapshot of the full student data before style change for EditItemCommand
                                full_old_student_data_for_style_cmd = s_current.copy()
                                full_old_student_data_for_style_cmd["style_overrides"] = current_style_snapshot

                                # The new_item_data_changes for EditItemCommand needs to be just the changes.
                                # Here, we are replacing the entire style_overrides dict from the template.
                                if current_style_snapshot != t_style_overrides:
                                     self.execute_command(EditItemCommand(self, target_student_id, "student", full_old_student_data_for_style_cmd, {"style_overrides": t_style_overrides.copy()}))


                    # Furniture (still by ID)
                    for item_id, t_data in template_furniture.items():
                         if item_id in self.furniture:
                            f_current = self.furniture[item_id]
                            old_x, old_y = f_current["x"], f_current["y"]
                            new_x, new_y = t_data.get("x", old_x), t_data.get("y", old_y)
                            if old_x != new_x or old_y != new_y : move_commands_data.append({'id':item_id, 'type':'furniture', 'old_x':old_x, 'old_y':old_y, 'new_x':new_x, 'new_y':new_y})

                            old_w = f_current.get("width", REBBI_DESK_WIDTH) ; old_h = f_current.get("height", REBBI_DESK_HEIGHT)
                            new_w = t_data.get("width", old_w); new_h = t_data.get("height", old_h)
                            if old_w != new_w or old_h != new_h: size_commands_data.append({'id':item_id, 'type':'furniture', 'old_w':old_w, 'old_h':old_h, 'new_w':new_w, 'new_h':new_h})

                    if move_commands_data: self.execute_command(MoveItemsCommand(self, move_commands_data))
                    if size_commands_data: self.execute_command(ChangeItemsSizeCommand(self, size_commands_data))

                    status_message = f"Layout '{os.path.basename(file_path)}' loaded. Applied to {applied_count} students."
                    if skipped_count > 0:
                        status_message += f" Skipped {skipped_count} template students (see console log for details)."
                    if name_match_log:
                        print("--- Layout Load Name Matching Log ---")
                        for log_line in name_match_log: print(log_line)
                        print("------------------------------------")

                    self.update_status(status_message)
                    self.draw_all_items(check_collisions_on_redraw=True)
                    self.save_data_wrapper(source="load_template")
            except (json.JSONDecodeError, IOError) as e: messagebox.showerror("Load Error", f"Could not load layout template: {e}", parent=self.root)
        else: self.update_status("Layout template load cancelled.")
        self.password_manager.record_activity()
    
    def generate_attendance_report_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Generate Report", "Enter password to generate attendance report:"): return

        dialog = AttendanceReportDialog(self.root, self.students)
        if dialog.result:
            start_date, end_date, selected_student_ids = dialog.result
            if not selected_student_ids:
                messagebox.showinfo("No Students", "No students selected for the report.", parent=self.root)
                return

            report_data = self.generate_attendance_data(start_date, end_date, selected_student_ids)
            if not report_data:
                messagebox.showinfo("No Data", "No attendance-relevant log data found for the selected criteria.", parent=self.root)
                return

            default_filename = f"attendance_report_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}.xlsx"
            file_path = filedialog.asksaveasfilename(defaultextension=".xlsx", initialfile=default_filename,
                                                   filetypes=[("Excel files", "*.xlsx"), ("All files", "*.*")], parent=self.root)
            if file_path:
                try:
                    self.export_attendance_to_excel(file_path, report_data, start_date, end_date)
                    self.update_status(f"Attendance report saved to {os.path.basename(file_path)}.")
                    if messagebox.askyesno("Export Successful", f"Attendance report saved to:\n{file_path}\n\nDo you want to open the file location?", parent=self.root):
                        self.open_specific_export_folder(file_path)
                except Exception as e:
                    messagebox.showerror("Export Error", f"Failed to save attendance report: {e}", parent=self.root)
            else:
                self.update_status("Attendance report export cancelled.")
        self.password_manager.record_activity()

    def generate_attendance_data(self, start_date, end_date, student_ids):
        attendance = {} # {date_obj: {student_id: "Present"}}
        all_logs = self.behavior_log + self.homework_log # Combine logs for presence check

        current_date = start_date
        while current_date <= end_date:
            attendance[current_date] = {}
            for student_id in student_ids:
                # Check if any log entry exists for this student on this date
                present = any(
                    log["student_id"] == student_id and
                    datetime.fromisoformat(log["timestamp"]).date() == current_date
                    for log in all_logs
                )
                attendance[current_date][student_id] = "P" if present else "A" # Present / Absent
            current_date += timedelta(days=1)
        return attendance

    def export_attendance_to_excel(self, file_path, attendance_data, report_start_date, report_end_date):
        wb = Workbook()
        ws = wb.active
        ws.title = "Attendance Report"

        headers = ["Student Name"]
        date_columns_map = {} # date_obj -> column_index
        current_col = 2
        d_iter = report_start_date
        while d_iter <= report_end_date:
            headers.append(d_iter.strftime("%Y-%m-%d (%a)"))
            date_columns_map[d_iter] = current_col
            current_col += 1
        headers.append("Total Present")
        headers.append("Total Absent")

        for col_num, header_title in enumerate(headers, 1):
            ws.cell(row=1, column=col_num, value=header_title).font = OpenpyxlFont(bold=True)
            ws.column_dimensions[get_column_letter(col_num)].width = 15 if col_num > 1 else 25
        ws.freeze_panes = 'B2'

        current_row = 2
        sorted_student_ids = sorted(
            list(set(sid for day_data in attendance_data.values() for sid in day_data.keys())),
            key=lambda sid: (self.students.get(sid, {}).get("last_name", ""), self.students.get(sid, {}).get("first_name", ""))
        )

        for student_id in sorted_student_ids:
            student_name = self.students.get(student_id, {}).get("full_name", student_id)
            ws.cell(row=current_row, column=1, value=student_name)
            total_present = 0
            total_absent = 0
            for date_obj, col_idx in date_columns_map.items():
                status = attendance_data.get(date_obj, {}).get(student_id, "A")
                ws.cell(row=current_row, column=col_idx, value=status).alignment = OpenpyxlAlignment(horizontal='center')
                if status == "P": total_present += 1
                else: total_absent += 1
            ws.cell(row=current_row, column=len(headers)-1, value=total_present).alignment = OpenpyxlAlignment(horizontal='center')
            ws.cell(row=current_row, column=len(headers), value=total_absent).alignment = OpenpyxlAlignment(horizontal='center')
            current_row += 1
        wb.save(file_path)

    def align_selected_items(self, edge):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Align", "Enter password to align items:"): return
        if len(self.selected_items) < 2:
            self.update_status("Select at least two items to align."); return

        items_data_for_align = []
        for item_id in self.selected_items:
            item_type = "student" if item_id in self.students else "furniture"
            data_source = self.students if item_type == "student" else self.furniture
            if item_id in data_source:
                item = data_source[item_id]
                items_data_for_align.append({
                    "id": item_id, "type": item_type, "x": item["x"], "y": item["y"],
                    "width": item.get("_current_world_width", item.get("width", DEFAULT_STUDENT_BOX_WIDTH)), # Use dynamic if available
                    "height": item.get("_current_world_height", item.get("height", DEFAULT_STUDENT_BOX_HEIGHT))
                })
        if not items_data_for_align: return

        target_coord = 0
        if edge == "left": target_coord = min(item["x"] for item in items_data_for_align)
        elif edge == "right": target_coord = max(item["x"] + item["width"] for item in items_data_for_align)
        elif edge == "top": target_coord = min(item["y"] for item in items_data_for_align)
        elif edge == "bottom": target_coord = max(item["y"] + item["height"] for item in items_data_for_align)
        elif edge == "center_h": # Align to average horizontal center of the selection box
            min_x = min(it["x"] for it in items_data_for_align)
            max_x_br = max(it["x"] + it["width"] for it in items_data_for_align)
            target_coord = (min_x + max_x_br) / 2 # This is the center of the bounding box of selected items
        elif edge == "center_v": # Align to average vertical center
            min_y = min(it["y"] for it in items_data_for_align)
            max_y_br = max(it["y"] + it["height"] for it in items_data_for_align)
            target_coord = (min_y + max_y_br) / 2

        move_commands_for_align = []
        for item_to_align in items_data_for_align:
            old_x_align, old_y_align = item_to_align["x"], item_to_align["y"]
            new_x_align, new_y_align = old_x_align, old_y_align
            if edge == "left": new_x_align = target_coord
            elif edge == "right": new_x_align = target_coord - item_to_align["width"]
            elif edge == "top": new_y_align = target_coord
            elif edge == "bottom": new_y_align = target_coord - item_to_align["height"]
            elif edge == "center_h": new_x_align = target_coord - item_to_align["width"] / 2
            elif edge == "center_v": new_y_align = target_coord - item_to_align["height"] / 2

            if abs(new_x_align - old_x_align) > 0.01 or abs(new_y_align - old_y_align) > 0.01:
                move_commands_for_align.append({'id': item_to_align["id"], 'type': item_to_align["type"], 'old_x': old_x_align, 'old_y': old_y_align, 'new_x': new_x_align, 'new_y': new_y_align})

        if move_commands_for_align:
            self.execute_command(MoveItemsCommand(self, move_commands_for_align))
            self.update_status(f"Aligned {len(move_commands_for_align)} items to {edge}.")
        else: self.update_status("Items already aligned."); self.draw_all_items(check_collisions_on_redraw=True)
        self.password_manager.record_activity()

    def distribute_selected_items_evenly(self, direction='horizontal'):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Distribute", "Enter password to distribute items:"): return

        if len(self.selected_items) < 2:
            self.update_status("Select at least two items to distribute.")
            return

        items_to_distribute = []
        for item_id in self.selected_items:
            item_data = None
            item_type = None
            default_width = DEFAULT_STUDENT_BOX_WIDTH
            default_height = DEFAULT_STUDENT_BOX_HEIGHT

            if item_id in self.students:
                item_data = self.students[item_id]
                item_type = "student"
            elif item_id in self.furniture:
                item_data = self.furniture[item_id]
                item_type = "furniture"
                # Furniture might have different defaults, but let's assume student defaults for now if not specified.
                # Or better, use specific furniture defaults if available.
                default_width = REBBI_DESK_WIDTH
                default_height = REBBI_DESK_HEIGHT


            if item_data:
                # Use _current_world_width/height if available (from draw_single_student/furniture)
                # otherwise fallback to item's own width/height or defaults.
                width = item_data.get('_current_world_width', item_data.get('width', default_width))
                height = item_data.get('_current_world_height', item_data.get('height', default_height))

                # For students, width/height might be in style_overrides
                if item_type == "student":
                    style_overrides = item_data.get("style_overrides", {})
                    width = style_overrides.get("width", width)
                    height = style_overrides.get("height", height)

                items_to_distribute.append({
                    "id": item_id,
                    "type": item_type,
                    "x": float(item_data["x"]),
                    "y": float(item_data["y"]),
                    "width": float(width),
                    "height": float(height),
                })

        if not items_to_distribute:
            return

        moves_for_command = []

        if direction == 'horizontal':
            items_to_distribute.sort(key=lambda item: item['x'])

            min_x_overall = items_to_distribute[0]['x']
            # Max x-coordinate is the x of the rightmost item's right edge
            max_x_item_overall = items_to_distribute[-1]
            max_x_coord_overall = max_x_item_overall['x'] + max_x_item_overall['width']

            total_items_width = sum(item['width'] for item in items_to_distribute)
            total_span = max_x_coord_overall - min_x_overall

            if len(items_to_distribute) > 1:
                available_space_for_gaps = total_span - total_items_width
                # Prevent negative gap if items overlap significantly; ensure a minimal positive gap or zero.
                gap_size = max(0, available_space_for_gaps / (len(items_to_distribute) - 1))
            else:
                return # Should be caught by len < 2 check

            current_x = min_x_overall # Start placing the first item at its original position (or the leftmost edge)
            for i, item in enumerate(items_to_distribute):
                # Only create a move command if the item's position actually changes
                if abs(item['x'] - current_x) > 0.01: # Using a small tolerance for float comparison
                    moves_for_command.append({
                        'id': item['id'], 'type': item['type'],
                        'old_x': item['x'], 'old_y': item['y'],
                        'new_x': current_x, 'new_y': item['y'] # Keep original y
                    })
                current_x += item['width'] + gap_size

        elif direction == 'vertical':
            items_to_distribute.sort(key=lambda item: item['y'])

            min_y_overall = items_to_distribute[0]['y']
            max_y_item_overall = items_to_distribute[-1]
            max_y_coord_overall = max_y_item_overall['y'] + max_y_item_overall['height']

            total_items_height = sum(item['height'] for item in items_to_distribute)
            total_span = max_y_coord_overall - min_y_overall

            if len(items_to_distribute) > 1:
                available_space_for_gaps = total_span - total_items_height
                gap_size = max(0, available_space_for_gaps / (len(items_to_distribute) - 1))
            else:
                return

            current_y = min_y_overall
            for i, item in enumerate(items_to_distribute):
                if abs(item['y'] - current_y) > 0.01:
                    moves_for_command.append({
                        'id': item['id'], 'type': item['type'],
                        'old_x': item['x'], 'old_y': item['y'],
                        'new_x': item['x'], 'new_y': current_y # Keep original x
                    })
                current_y += item['height'] + gap_size

        if moves_for_command:
            self.execute_command(MoveItemsCommand(self, moves_for_command))
            self.update_status(f"Distributed {len(moves_for_command)} items {direction}ly.")
        else:
            self.update_status(f"Items already distributed {direction}ly or no change needed.")
        self.password_manager.record_activity()

    def assign_student_to_group_via_menu(self, student_id, group_id):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Assign Group", "Enter password to assign group:"): return
        student = self.students.get(student_id)
        if not student: return

        old_group_id = student.get("group_id")
        new_group_id_val = None if group_id == "NONE_GROUP_SENTINEL" else group_id

        if old_group_id != new_group_id_val:
            # For undo/redo, we need snapshots of all group assignments if ManageStudentGroupCommand is used broadly.
            # For a single assignment, EditItemCommand is simpler.
            old_student_data_snapshot = student.copy()
            if "style_overrides" in old_student_data_snapshot: old_student_data_snapshot["style_overrides"] = old_student_data_snapshot["style_overrides"].copy()
            
            changes_for_command = {"group_id": new_group_id_val}
            self.execute_command(EditItemCommand(self, student_id, "student", old_student_data_snapshot, changes_for_command))
            # Command will call draw_all_items, which will redraw the student
            group_name = self.student_groups[new_group_id_val]['name'] if new_group_id_val and new_group_id_val in self.student_groups else "No Group"
            self.update_status(f"Assigned {student['full_name']} to group: {group_name}.")
            self.save_data_wrapper(source="assign_group_menu") # Save student data which now includes group_id
        self.password_manager.record_activity()

    def assign_students_to_group_via_menu(self, student_ids, group_id):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Assign Group", "Enter password to assign group:"): return
        
        for student in student_ids:
            student_id = student
            student = self.students.get(student_id)
            if not student: return

            old_group_id = student.get("group_id")
            new_group_id_val = None if group_id == "NONE_GROUP_SENTINEL" else group_id

            if old_group_id != new_group_id_val:
                # For undo/redo, we need snapshots of all group assignments if ManageStudentGroupCommand is used broadly.
                # For a single assignment, EditItemCommand is simpler.
                old_student_data_snapshot = student.copy()
                if "style_overrides" in old_student_data_snapshot: old_student_data_snapshot["style_overrides"] = old_student_data_snapshot["style_overrides"].copy()
                
                changes_for_command = {"group_id": new_group_id_val}
                self.execute_command(EditItemCommand(self, student_id, "student", old_student_data_snapshot, changes_for_command))
                # Command will call draw_all_items, which will redraw the student
                group_name = self.student_groups[new_group_id_val]['name'] if new_group_id_val and new_group_id_val in self.student_groups else "No Group"
                self.update_status(f"Assigned {student['full_name']} to group: {group_name}.")
                self.save_data_wrapper(source="assign_group_menu") # Save student data which now includes group_id
        self.password_manager.record_activity()

    def manage_student_groups_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Manage Groups", "Enter password to manage student groups:"): return
        # Take snapshots for ManageStudentGroupCommand
        old_groups_snap = {gid: gdata.copy() for gid, gdata in self.student_groups.items()}
        old_student_assignments_snap = {sid: sdata.get("group_id") for sid, sdata in self.students.items() if "group_id" in sdata and sdata["group_id"] is not None}
        old_next_group_id_num_snap = self.next_group_id_num

        dialog = ManageStudentGroupsDialog(self.root, self.student_groups, self.students, self, default_colors=DEFAULT_GROUP_COLORS)
        if dialog.groups_changed_flag: # Check if dialog indicated changes were made
            # The dialog directly modifies self.student_groups, self.students (group_id), and self.next_group_id_num
            new_groups_snap = {gid: gdata.copy() for gid, gdata in self.student_groups.items()} # Current state after dialog
            new_student_assignments_snap = {sid: sdata.get("group_id") for sid, sdata in self.students.items() if "group_id" in sdata and sdata["group_id"] is not None}
            new_next_group_id_num_snap = self.next_group_id_num

            cmd = ManageStudentGroupCommand(self, old_groups_snap, new_groups_snap,
                                            old_student_assignments_snap, new_student_assignments_snap,
                                            old_next_group_id_num_snap, new_next_group_id_num_snap)
            # Execute command will apply the new state (which is already set by dialog) and handle saving/drawing
            # The command's execute() will effectively re-apply what the dialog did, which is fine.
            # The crucial part is that undo() will restore the old snapshots.
            self.execute_command(cmd) # This will also call save_student_groups and draw_all_items
            self.update_status("Student groups updated via dialog.") # Status from command is more generic
        else:
            self.update_status("Manage student groups cancelled or no changes made.")
        self.password_manager.record_activity()

    def toggle_student_groups_ui_visibility(self):
        enabled = self.settings.get("student_groups_enabled", True)
        if hasattr(self, 'manage_groups_btn'):
            self.manage_groups_btn.config(state=tk.NORMAL if enabled else tk.DISABLED)
        self.draw_all_items(check_collisions_on_redraw=False) # Redraw to show/hide indicators

    def toggle_manage_boxes_visibility(self):
        if self.edit_mode_var.get() or self.settings.get("always_show_box_management", False): self.top_controls_frame_row2.pack(side=tk.TOP, fill=tk.X, pady=(2, 5)); self.top_frame.height_adjusted = 110
        else: self.top_controls_frame_row2.pack_forget(); self.top_frame.height_adjusted = 50
            
    def manage_quiz_templates_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Manage Templates", "Enter password to manage quiz templates:"): return
        dialog = ManageQuizTemplatesDialog(self.root, self) # Pass app instance
        if dialog.templates_changed_flag:
            self.save_quiz_templates() # Dialog modifies self.quiz_templates directly for now
            self.update_status("Quiz templates updated.")
        else:
            self.update_status("Quiz template management cancelled or no changes made.")
        self.password_manager.record_activity()

    def manage_homework_templates_dialog(self): # New
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Manage Templates", "Enter password to manage homework templates:"): return
        dialog = ManageHomeworkTemplatesDialog(self.root, self) # Pass app instance
        if dialog.templates_changed_flag:
            self.save_homework_templates() # Dialog modifies self.homework_templates directly
            self.update_status("Homework templates updated.")
        else:
            self.update_status("Homework template management cancelled or no changes made.")
        self.password_manager.record_activity()
    
    def set_theme(self, theme, canvas_color):
        self.theme_style_using = theme
        self.theme_set()
        
        if canvas_color == "Default" or canvas_color == "" or canvas_color == None:
            canvas_color = None; self.custom_canvas_color = None
        else:
            self.custom_canvas_color = canvas_color
            self.canvas_color = canvas_color
        
        if self.custom_canvas_color: self.canvas_color = self.custom_canvas_color
        elif self.theme_style_using == "Dark": self.canvas_color = "#1F1F1F"
        elif self.theme_style_using == "System": self.canvas_color = "lightgrey" if darkdetect.theme() == "Light" else "#1F1F1F"
        else: self.canvas_color = "lightgrey"
        self.canvas.configure(bg=self.canvas_color)
    
    def _apply_canvas_color(self):
        """Applies the current canvas color based on theme and custom settings."""
        if self.custom_canvas_color and self.custom_canvas_color != "Default":
            self.canvas_color = self.custom_canvas_color
        elif self.theme_style_using == "Dark":
            self.canvas_color = "#1F1F1F"
        elif self.theme_style_using == "System":
            self.canvas_color = "lightgrey" if darkdetect.theme() == "Light" else "#1F1F1F"
        else: # Light theme
            self.canvas_color = "lightgrey"
        
        if hasattr(self, 'canvas') and self.canvas:
            self.canvas.configure(bg=self.canvas_color)
    
    def theme_set(self, theme=None): 
        if self.type_theme == "sv_ttk":
            if self.theme_style_using == "System":
                sv_ttk.set_theme(darkdetect.theme())
            else:
                if self.theme_style_using.lower() == "light" or self.theme_style_using.lower() == "dark":
                    sv_ttk.set_theme(self.theme_style_using)
                else:
                    self.theme_style_using = "Light"
                    sv_ttk.set_theme(self.theme_style_using)
        else:
            style = ttk.Style(self.root)
            
            style.theme_use(self.type_theme if "sun-valley" not in self.type_theme else f"{self.type_theme[:10]}-{self.theme_style_using.lower()}")
    
    def theme_auto(self, init=False):
        self.theme_set()
        if self.custom_canvas_color != "Default" and self.custom_canvas_color != None: self.canvas_color = self.custom_canvas_color
        elif self.theme_style_using == "Dark": self.canvas_color = "#1F1F1F"
        elif self.theme_style_using == "System": self.canvas_color = "lightgrey" if darkdetect.theme() == "Light" else "#1F1F1F"
        else: self.canvas_color = "lightgrey"
        
        if not init == True:
            print(init, self.canvas_color, self.custom_canvas_color)
            self.canvas.configure(bg=self.canvas_color) # type: ignore

    def open_settings_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Open Settings", "Enter password to open settings:"): return
        style = ttk.Style(self.root)
        self.styles = style.theme_names()
        # Store a copy of settings for potential revert or detailed change tracking for undo (complex)
        # For now, settings dialog applies changes directly and saves.
        # old_settings_snapshot = self.settings.copy() # For a potential future SettingsChangeCommand
        
        # Unbind root so that shorcuts can work in settings
        self.root.unbind_all("<Control-z>")
        self.root.unbind_all("<Control-y>")
        self.root.unbind_all("<Control-Shift-Z>")
        
        dialog = SettingsDialog(self.root, self.settings, self.custom_behaviors, self.all_behaviors, self,
                                self.custom_homework_statuses, self.all_homework_statuses, # Homework log behaviors
                                self.custom_homework_types, self.all_homework_session_types, # Homework session types (Yes/No mode)
                                self.password_manager, self.theme_style_using, self.custom_canvas_color, self.styles, self.type_theme)
        if dialog.settings_changed_flag: # Check if dialog indicated changes
            # Settings are applied directly by the dialog for most parts
            self.save_data_wrapper(source="settings_dialog") # Save all data as settings are part of it
            self.update_all_behaviors(); self.update_all_homework_log_behaviors(); self.update_all_homework_session_types()
            self.guide_line_color = self.settings.get("guides_color", "blue")
            self.draw_all_items(check_collisions_on_redraw=True)
            self.update_status("Settings updated.")
            self._update_toggle_dragging_button_text()
            self.update_zoom_display()
            self.update_lock_button_state()
            self.toggle_student_groups_ui_visibility()
            self.set_theme(self.theme_style_using, self.custom_canvas_color)
            self.toggle_manage_boxes_visibility()
            
            # Re-schedule autosave if interval changed
            self.root.after_cancel(self.autosave_data_wrapper) # Cancel existing if any (might need to store the after_id)
            self.root.after(self.settings.get("autosave_interval_ms", 30000), self.autosave_data_wrapper)
        else:
            try:
                self.update_status("Settings dialog closed, no changes applied through dialog confirm.")
            except: pass
        
        # Rebind root after settings closes
        self.root.bind_all("<Control-z>", lambda event: self.undo_last_action())
        self.root.bind_all("<Control-y>", lambda event: self.redo_last_action())
        self.root.bind_all("<Control-Shift-Z>", lambda event: self.redo_last_action()) # Common alternative for redo        

        self.password_manager.record_activity()

    def reset_application_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Reset Application", "Enter password to reset application:"): return
        msg = "This will reset ALL application data including students, furniture, logs, settings, custom behaviors, templates, and groups. This action CANNOT be undone.\n\nAre you absolutely sure you want to reset the application to its default state?"
        if messagebox.askyesno("Confirm Application Reset", msg, icon='error', parent=self.root, default=messagebox.NO):
            # Second, more direct confirmation
            if messagebox.askyesno("Final Confirmation", "Really reset everything? This is your last chance to cancel.", icon='error', parent=self.root, default=messagebox.NO):
                self._perform_reset()
            else: self.update_status("Application reset cancelled (final prompt).")
        else: self.update_status("Application reset cancelled.")
        self.password_manager.record_activity()

    def _perform_reset(self):
        try:
            self.backup_all_data_dialog(force=True)
        except Exception as e:
            print(e)
        try:
            # Clear current data in memory
            self.students.clear(); self.furniture.clear(); self.behavior_log.clear(); self.homework_log.clear()
            self.student_groups.clear(); self.quiz_templates.clear(); self.homework_templates.clear()
            self.custom_behaviors.clear(); self.custom_homework_statuses.clear(); #self.custom_homework_session_types.clear()
            self.undo_stack.clear(); self.redo_stack.clear()
            self._per_student_last_cleared.clear()
            self.last_excel_export_path = None
            self.settings = self._get_default_settings() # Reset to defaults
            self._ensure_next_ids() # Reset ID counters based on default settings
            self.password_manager = PasswordManager(self.settings) # Reset password manager with fresh settings
            self.guides.clear()
            # Delete data files
            files_to_delete = [
                DATA_FILE, CUSTOM_BEHAVIORS_FILE, 
                CUSTOM_HOMEWORK_TYPES_FILE, # NEW
                CUSTOM_HOMEWORK_STATUSES_FILE, # RENAMED
                STUDENT_GROUPS_FILE, QUIZ_TEMPLATES_FILE, HOMEWORK_TEMPLATES_FILE,
                AUTOSAVE_EXCEL_FILE
            ]
            # Attempt to delete old version files if they exist from previous versions
            for i in range(1, int(CURRENT_DATA_VERSION_TAG[1:])):
                files_to_delete.append(get_app_data_path(f"classroom_data_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"custom_behaviors_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"custom_homeworks_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"student_groups_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"quiz_templates_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"homework_templates_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"custom_homework_session_types_v{i}.json"))
                files_to_delete.append(get_app_data_path(f"autosave_log_v{i}.xlsx"))


            for f_path in files_to_delete:
                if os.path.exists(f_path):
                    try: os.remove(f_path)
                    except OSError as e: print(f"Warning: Could not delete file {f_path} during reset: {e}")
            
            # Delete layout templates directory contents
            if os.path.exists(LAYOUT_TEMPLATES_DIR):
                for item_name in os.listdir(LAYOUT_TEMPLATES_DIR):
                    item_path = os.path.join(LAYOUT_TEMPLATES_DIR, item_name)
                    try:
                        if os.path.isfile(item_path) or os.path.islink(item_path): os.unlink(item_path)
                        elif os.path.isdir(item_path): shutil.rmtree(item_path)
                    except Exception as e_del_layout: print(f"Failed to delete {item_path}: {e_del_layout}")


            # Save fresh default data (which will create new empty files)
            self.save_data_wrapper(source="reset")
            self.update_all_behaviors(); self.update_all_homework_statuses(); self.update_all_homework_session_types()
            self.draw_all_items(check_collisions_on_redraw=True)
            self.update_undo_redo_buttons_state()
            self.update_lock_button_state()
            self.update_status("Application has been reset to default state.")
            messagebox.showinfo("Reset Complete", "Application reset successfully. All data and settings are now at their defaults.", parent=self.root)
        except Exception as e:
            messagebox.showerror("Reset Error", f"An error occurred during application reset: {e}", parent=self.root)
            self.update_status(f"Error during reset: {e}")

    def backup_all_data_dialog(self, force=False):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Backup", "Enter password to create a backup:"): return
        default_filename = f"{APP_NAME}_Backup_{CURRENT_DATA_VERSION_TAG}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.zip"
        if force == False:
            backup_zip_path = filedialog.asksaveasfilename(
                title="Save Backup As",
                defaultextension=".zip",
                initialfile=default_filename,
                filetypes=[("ZIP archive", "*.zip")],
                parent=self.root
            )
            if not backup_zip_path:
                self.update_status("Backup cancelled."); return
        elif force == True:
            backup_zip_path = os.path.abspath(os.path.join(os.path.dirname(DATA_FILE), default_filename))
        # Ensure latest data is saved before backup
        self.save_data_wrapper(source="backup_preparation")

        files_to_backup = [
            DATA_FILE, CUSTOM_BEHAVIORS_FILE, 
            CUSTOM_HOMEWORK_TYPES_FILE, # NEW
            CUSTOM_HOMEWORK_STATUSES_FILE, # RENAMED
            STUDENT_GROUPS_FILE,
            QUIZ_TEMPLATES_FILE, HOMEWORK_TEMPLATES_FILE,
        ]
        # Also include all files in LAYOUT_TEMPLATES_DIR
        layout_template_files = []
        if os.path.exists(LAYOUT_TEMPLATES_DIR):
            for fname in os.listdir(LAYOUT_TEMPLATES_DIR):
                fpath = os.path.join(LAYOUT_TEMPLATES_DIR, fname)
                if os.path.isfile(fpath): layout_template_files.append(fpath)
        
        try:
            with zipfile.ZipFile(backup_zip_path, 'w', zipfile.ZIP_DEFLATED) as zf:
                for file_path in files_to_backup:
                    if os.path.exists(file_path) and os.path.isfile(file_path):
                        zf.write(file_path, arcname=os.path.basename(file_path))
                for file_path in layout_template_files:
                     zf.write(file_path, arcname=os.path.join(LAYOUT_TEMPLATES_DIR_NAME, os.path.basename(file_path)))
            self.update_status(f"Backup created: {os.path.basename(backup_zip_path)}")
            messagebox.showinfo("Backup Successful", f"All application data backed up to:\n{backup_zip_path}", parent=self.root)
        except Exception as e:
            messagebox.showerror("Backup Error", f"Failed to create backup: {e}", parent=self.root)
            self.update_status(f"Error creating backup: {e}")
        finally:
            self.password_manager.record_activity()

    def restore_all_data_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Restore", "Enter password to restore data:"): return

        if not messagebox.askyesno("Confirm Restore", "Restoring data will OVERWRITE all current application data (students, logs, settings, etc.) with the contents of the backup.\nThis action CANNOT BE UNDONE directly from the backup itself.\n\nAre you sure you want to proceed?", parent=self.root, icon='warning', default=messagebox.NO):
            self.update_status("Restore cancelled."); return

        backup_zip_path = filedialog.askopenfilename(
            title="Select Backup File to Restore",
            filetypes=[("ZIP archives", "*.zip")],
            parent=self.root
        )
        if not backup_zip_path:
            self.update_status("Restore cancelled."); return

        app_data_dir = os.path.dirname(DATA_FILE) # Get the directory where app data is stored
        
        try:
            with zipfile.ZipFile(backup_zip_path, 'r') as zf:
                # Preliminary check for main data file to guess version compatibility (optional)
                main_data_filename_in_zip = None
                expected_main_data_filename = os.path.basename(DATA_FILE) # e.g. classroom_data_v9.json
                
                # List of all possible data file names across versions to check against
                possible_main_data_files = [f"classroom_data_v{i}.json" for i in range(1,11)] + ["classroom_data.json"] # Make sure to make the range one above the current data version tag, so that it catches itself

                found_compatible_main_file = False
                for name_in_zip in zf.namelist():
                    if os.path.basename(name_in_zip) in possible_main_data_files:
                        main_data_filename_in_zip = os.path.basename(name_in_zip)
                        found_compatible_main_file = True
                        break
                
                if not found_compatible_main_file:
                    messagebox.showerror("Restore Error", "The selected ZIP file does not appear to be a valid application backup (missing main data file).", parent=self.root)
                    return

                # Clear existing layout templates before extraction
                if os.path.exists(LAYOUT_TEMPLATES_DIR):
                    for item_name in os.listdir(LAYOUT_TEMPLATES_DIR):
                        item_path = os.path.join(LAYOUT_TEMPLATES_DIR, item_name)
                        try:
                            if os.path.isfile(item_path) or os.path.islink(item_path): os.unlink(item_path)
                            elif os.path.isdir(item_path): shutil.rmtree(item_path)
                        except Exception as e_del_layout: print(f"Failed to delete old layout item {item_path}: {e_del_layout}")
                else:
                    os.makedirs(LAYOUT_TEMPLATES_DIR, exist_ok=True)


                # Extract files directly into the application data directory
                # This will overwrite existing files with the same names.
                for member in zf.infolist():
                    # Handle paths correctly: extract to app_data_dir, but if member.filename includes a dir (like layout_templates), recreate that structure
                    target_path = os.path.join(app_data_dir, member.filename)
                    # Ensure parent directory exists for the target_path
                    target_dir_for_file = os.path.dirname(target_path)
                    if not os.path.exists(target_dir_for_file):
                        os.makedirs(target_dir_for_file, exist_ok=True)

                    if not member.is_dir(): # Check if it's a file
                        with open(target_path, "wb") as outfile:
                            outfile.write(zf.read(member.filename))
            
            # After extraction, reload data from the potentially new main data file.
            # The main data file name in the backup might be an older version.
            # The load_data method handles migration.
            path_to_load_after_restore = os.path.join(app_data_dir, main_data_filename_in_zip) if main_data_filename_in_zip else DATA_FILE

            self.load_data(file_path=path_to_load_after_restore, is_restore=True) # Reload all data from extracted files
            self.load_custom_behaviors(); self.load_custom_homework_statuses(); #self.load_custom_homework_session_types()
            self.load_student_groups(); self.load_quiz_templates(); self.load_homework_templates()
            self._ensure_next_ids() # Crucial after loading potentially old data
            self.update_all_behaviors(); self.update_all_homework_log_behaviors(); self.update_all_homework_session_types()
            self.draw_all_items(check_collisions_on_redraw=True)
            self.update_undo_redo_buttons_state()
            self.update_lock_button_state()
            self.toggle_student_groups_ui_visibility()
            self.mode_var.set(self.settings.get("current_mode", "behavior")); self.toggle_mode()


            self.update_status("Data restored successfully. Application reloaded.")
            messagebox.showinfo("Restore Successful", "Data restored from backup. The application has reloaded the restored data.", parent=self.root)

        except FileNotFoundError:
            messagebox.showerror("Restore Error", "Backup file not found.", parent=self.root)
        except zipfile.BadZipFile:
            messagebox.showerror("Restore Error", "Invalid or corrupted backup ZIP file.", parent=self.root)
        except Exception as e:
            messagebox.showerror("Restore Error", f"Failed to restore data: {e}", parent=self.root)
            self.update_status(f"Error restoring data: {e}")
            # Attempt to reload current (pre-restore attempt) data to stabilize
            self.load_data(DATA_FILE, is_restore=False)
            self.draw_all_items()
        finally:
            self.password_manager.record_activity()

    def open_data_folder(self):
        folder_path = os.path.dirname(DATA_FILE)
        try:
            if sys.platform == "win32": os.startfile(folder_path)
            elif sys.platform == "darwin": subprocess.Popen(["open", folder_path])
            else: subprocess.Popen(["xdg-open", folder_path])
            self.update_status(f"Opened data folder: {folder_path}")
        except Exception as e:
            self.update_status(f"Error opening data folder: {e}")
            messagebox.showerror("Error", f"Could not open data folder: {e}\nPath: {folder_path}", parent=self.root)

    def open_last_export_folder(self):
        if self.last_excel_export_path and os.path.exists(os.path.dirname(self.last_excel_export_path)):
            self.open_specific_export_folder(self.last_excel_export_path)
        else: self.update_status("Last export path not set or not found.")

    def open_specific_export_folder(self, file_path_in_folder):
        folder_path = os.path.dirname(file_path_in_folder)
        try:
            if sys.platform == "win32": os.startfile(folder_path)
            elif sys.platform == "darwin": subprocess.Popen(["open", folder_path])
            else: subprocess.Popen(["xdg-open", folder_path])
            self.update_status(f"Opened folder: {folder_path}")
        except Exception as e:
            self.update_status(f"Error opening folder {folder_path}: {e}")
            messagebox.showerror("Error", f"Could not open folder: {e}\nPath: {folder_path}", parent=self.root)

    def show_help_dialog(self):
        HelpDialog(self.root, APP_VERSION)

    def show_undo_history_dialog(self):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to View History", "Enter password to view undo history:"): return
        # Ensure dialogs module is available where UndoHistoryDialog is defined
        from undohistorydialog import UndoHistoryDialog
        # Check if a dialog is already open, if so, bring to front or recreate
        if hasattr(self, '_undo_history_dialog_instance') and self._undo_history_dialog_instance.winfo_exists():
            self._undo_history_dialog_instance.lift()
            self._undo_history_dialog_instance.populate_history() # Refresh content
        else:
            self._undo_history_dialog_instance = UndoHistoryDialog(self.root, self)
        self.password_manager.record_activity()

    def selective_redo_action(self, target_command_index_in_undo_stack):
        if self.password_manager.is_locked:
            if not self.prompt_for_password("Unlock to Redo Action", "Enter password to perform this redo action:"):
                return

        if not (0 <= target_command_index_in_undo_stack < len(self.undo_stack)):
            messagebox.showerror("Error", "Invalid action selected for redo.", parent=self.root)
            return

        # Commands to be undone to reach the target command (these come after the target in execution order)
        commands_to_undo_count = len(self.undo_stack) - 1 - target_command_index_in_undo_stack

        temp_undone_for_redo_stack = []

        # 1. Undo actions that occurred *after* the target command
        for _ in range(commands_to_undo_count):
            if not self.undo_stack: break # Should not happen if logic is correct
            command_to_temporarily_undo = self.undo_stack.pop()
            try:
                command_to_temporarily_undo.undo()
                temp_undone_for_redo_stack.append(command_to_temporarily_undo) # Keep them in order of undoin
            except Exception as e:
                messagebox.showerror("Selective Redo Error", f"Error undoing a subsequent action: {e}", parent=self.root)
                # Attempt to restore state might be complex; for now, stop and alert user.
                # Re-push commands that were successfully undone before error?
                # Or, more simply, acknowledge that the state might be partially changed.
                self.undo_stack.append(command_to_temporarily_undo) # Put it back if undo failed
                for cmd_to_re_push in reversed(temp_undone_for_redo_stack): # Re-push successfully undone ones
                    self.undo_stack.append(cmd_to_re_push)
                self.draw_all_items(check_collisions_on_redraw=True)
                return

        # 2. The target command is now at the top of the undo_stack. Pop it.
        if not self.undo_stack or len(self.undo_stack) -1 != target_command_index_in_undo_stack :
             messagebox.showerror("Error", "Undo stack state error during selective redo.", parent=self.root)
             # Restore temp_undone_for_redo_stack to undo_stack before returning
             for cmd_to_re_push in reversed(temp_undone_for_redo_stack): self.undo_stack.append(cmd_to_re_push)
             return

        target_command = self.undo_stack.pop()

        # 3. Undo the target command itself (to get its original pre-state for redo, and add to redo_stack)
        try:
            target_command.undo()
            # self.redo_stack.append(target_command) # Standard undo would do this.
                                                  # For selective redo, we are immediately re-executing it.
                                                  # The key is that subsequent history is invalidated.
        except Exception as e:
            messagebox.showerror("Selective Redo Error", f"Error undoing the target action: {e}", parent=self.root)
            self.undo_stack.append(target_command) # Put target back
            for cmd_to_re_push in reversed(temp_undone_for_redo_stack): self.undo_stack.append(cmd_to_re_push) # Put subsequent back
            self.draw_all_items(check_collisions_on_redraw=True)
            return

        # 4. Re-execute the target command
        try:
            target_command.execute()
            self.undo_stack.append(target_command) # Add it back to the undo_stack as the new latest action
        except Exception as e:
            messagebox.showerror("Selective Redo Error", f"Error re-executing the target action: {e}", parent=self.root)
            # State might be inconsistent. Try to restore the target command to its "undone" state.
            # This is tricky. Simplest is to inform user.
            # For now, we'll leave it as executed on the undo_stack and let user manually undo if needed.
            self.draw_all_items(check_collisions_on_redraw=True)
            return

        # 5. Invalidate subsequent history: Clear the redo_stack and the temp_undone_for_redo_stack is discarded.
        self.redo_stack.clear()
        # temp_undone_for_redo_stack is naturally discarded as it's a local variable.
        # These actions are now "lost" as a new history branch has been created.

        self.update_status(f"Redid action: {target_command.get_description()}. Subsequent history cleared.")
        self.draw_all_items(check_collisions_on_redraw=True)
        self.save_data_wrapper(source="selective_redo")
        self.password_manager.record_activity()
        # The UndoHistoryDialog should refresh itself.

    def on_exit_protocol(self, force_quit=False):

        #dialog = ExitConfirmationDialog(self.root, "Exit Confirmation")
        #if dialog.result == "save_quit":
        #    self.save_data_wrapper(source="exit_save")
        #    self.root.destroy()
        #elif dialog.result == "no_save_quit":
        #    self.root.destroy()
        # If dialog.result is None (Cancel), do nothing.
        
        try:
            if not force_quit:
                if self.password_manager.is_locked:
                    if not self.prompt_for_password("Unlock to Save & Quit", "Enter password to save and quit:"): return

                if self.is_live_quiz_active and not self.prompt_end_live_session_on_mode_switch("quiz"): return
                if self.is_live_homework_active and not self.prompt_end_live_session_on_mode_switch("homework"): return

                #if messagebox.askyesno("Exit", "Save changes and exit application?", parent=self.root, ):
                #    self.save_data_wrapper(source="exit_protocol")
                #else: # User chose not to save, but still wants to exit
                #    self.update_status("Exited without saving.")
                dialog = ExitConfirmationDialog(self.root, "Exit Confirmation")
                if dialog.result == "save_quit":
                    self.save_data_wrapper(source="exit_protocol")
                    self.root.destroy()
                    sys.exit(0) # Ensure clean exit
                elif dialog.result == "no_save_quit":
                    #if self.file_lock_manager: self.file_lock_manager.release_lock()
                    self.update_status("Exited without saving.")
                    self.root.destroy()
                    
                    sys.exit(0) # Ensure clean exit
            else: # Force quit (e.g. after save_and_quit or if lock fails)
                self.root.destroy()
                sys.exit(0) # Ensure clean exit # Data should have been saved by save_and_quit if called from there
        except Exception as e:
            print(f"Error during exit procedure: {e}") # Log error but proceed with exit
            self.root.destroy()
            sys.exit(0) # Ensure clean exit
        #finally:
        #    
        #    self.root.destroy()
        #    sys.exit(0) # Ensure clean exit

class ScrollableToolbar(ttk.Frame):
    """A horizontally scrollable frame, used for toolbars."""
    def __init__(self, parent, *args, **kwargs):
        super().__init__(parent, *args, **kwargs)
        
        self.height_adjusted = 50
        # The canvas that will contain the scrollable content
        self.canvas = tk.Canvas(self, highlightthickness=0, borderwidth=0, height=self.height_adjusted)
        
        # The scrollbar
        self.scrollbar = ttk.Scrollbar(self, orient="horizontal", command=self.canvas.xview)
        
        # The interior frame to hold the widgets
        self.interior = ttk.Frame(self.canvas)

        # Place the scrollbar and canvas
        self.scrollbar.pack(side=tk.BOTTOM, fill=tk.X)
        self.canvas.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        # Link canvas to scrollbar
        self.canvas.configure(xscrollcommand=self.scrollbar.set)

        # Create a window in the canvas that contains the interior frame
        self.canvas.create_window((0, 0), window=self.interior, anchor="nw")

        # Update scrollregion when the interior frame's size changes
        self.interior.bind('<Configure>', self._on_frame_configure)

        # Bind mousewheel scrolling for convenience
        self.canvas.bind('<Enter>', self._bind_mousewheel)
        self.canvas.bind('<Leave>', self._unbind_mousewheel)

    def _on_frame_configure(self, event=None):
        """Update the scrollregion of the canvas."""
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))
        self.canvas.configure(height=self.height_adjusted)

    def _bind_mousewheel(self, event):
        """Bind mousewheel for horizontal scrolling."""
        self.canvas.bind_all("<MouseWheel>", self._on_mousewheel)

    def _unbind_mousewheel(self, event):
        """Unbind mousewheel."""
        self.canvas.unbind_all("<MouseWheel>")

    def _on_mousewheel(self, event):
        """Handle horizontal scrolling with the mousewheel."""
        self.canvas.xview_scroll(int(-1 * (event.delta / 120)), "units")




# --- Main Execution ---
if __name__ == "__main__":
    try:
        import pyi_splash
        # You can optionally update the splash screen text as things load
        pyi_splash.update_text("Loading UI...")
    except ImportError:
        pyi_splash = None # Will be None when not running from a PyInstaller bundle
    except RuntimeError: pass

    root = tk.Tk()
    # Apply a theme if available and desired
    try:
        # Examples: 'clam', 'alt', 'default', 'classic'
        # Some themes might require python -m tkinter to see available ones on your system
        # Or use ttkthemes for more options: from ttkthemes import ThemedTk
        # root = ThemedTk(theme="arc") # Example using ttkthemes
        style = ttk.Style(root)
        #available_themes = style.theme_names() # ('winnative', 'clam', 'alt', 'default', 'classic', 'vista', 'xpnative') on Windows
        # print("Available themes:", available_themes)
        sv_ttk.set_theme("Light")
        #if 'vista' in available_themes: style.theme_use('vista')
        #elif 'xpnative' in available_themes: style.theme_use('xpnative')

    except Exception as e_theme:
        print(f"Could not apply custom theme: {e_theme}")
        
    app = SeatingChartApp(root)
    
    try:
        t = threading.Thread(target=darkdetect.listener, args=(app.theme_auto, ))
        t.daemon = True
        t.start()
    except: pass

    # Close the splash screen once the main app is initialized and ready
    try: pyi_splash.close()
    except: pass
    
    root.mainloop()