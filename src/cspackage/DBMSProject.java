package cspackage;

import java.sql.*;
import java.util.Scanner;

public class DBMSProject {
    private static Connection conn;

    public static void main(String[] args) {
        try {
        	Scanner s = new Scanner(System.in);
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/colinmolina?sslmode=require");
            boolean DBMS = true;
            
            while (DBMS) {
                System.out.println("Menu:");
                System.out.println("1 - Get transcript");
                System.out.println("2 - Register for a course");
                System.out.println("3 - Post grade");
                System.out.println("Q - Quit");
                System.out.print("Choice: ");
                String choice = s.nextLine().trim().toLowerCase();

                switch (choice) {
                    case "1":
                    	getTranscript(s);                       
                        break;
                    case "2":
                    	registerStudent(s);
                        break;
                    case "3":
                    	postGrade(s);
                        break;
                    case "q":
                        DBMS = false;
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            }

            conn.close();
            System.out.println("Shutting down course system.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void getTranscript(Scanner s) throws Exception {		 
    	String studentList = """		
    	        			 SELECT r.student_id, p.last_name, p.first_name
    	        			 FROM People p
    	        			 JOIN Registrations r ON p.person_id = r.student_id
    	        			 ORDER BY r.student_id
    	        			 """; //probably would've been faster to make a view for all of these, but you live and learn
    	try (PreparedStatement st = conn.prepareStatement(studentList); 
    			ResultSet rs = st.executeQuery()) {
            /**
    		 * GENERATIVE AI USED FOR TABLE FORMATTING:
    		 * System.out.println(String.format("Student ID=%d Last=%s First=%s",
                             rs.getInt("student_id"), rs.getString("last_name"), rs.getString("first_name")
                     ));

    				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
    		 */
    		System.out.println(String.format("+------------+------------------+------------------+"));
            System.out.println(String.format("| Student ID | Last Name        | First Name       |"));
            System.out.println(String.format("+------------+------------------+------------------+"));

            while (rs.next()) {
                System.out.println(String.format("| %-10d | %-16s | %-16s |",
                        rs.getInt("student_id"), rs.getString("last_name"), rs.getString("first_name")));
            }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        System.out.print("Enter your Student ID: ");
        int studentId = Integer.parseInt(s.nextLine());

        String transcript = """
        					SELECT s.term, c.credits, c.course_id, c.course_name, r.grade
        					FROM Courses c
        					JOIN Sections s ON c.course_id = s.course_id
        					JOIN Registrations r ON s.section_id = r.section_id
        					WHERE r.student_id = ?
        					ORDER BY s.term DESC
        					""";

        try (PreparedStatement st = conn.prepareStatement(transcript)) {
            st.setInt(1, studentId);
            ResultSet rs = st.executeQuery();

            System.out.println("Transcript:");
            /**
    		 * GENERATIVE AI USED FOR TABLE FORMATTING:
    		 * System.out.println(String.format("Term=%s Credits=%d Course ID=%d Course Name=%s Grade=%s",
                        rs.getString("term"), rs.getInt("credits"), rs.getInt("course_id"), rs.getString("course_name"), rs.getString("grade")
                ));

    				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
    		 */
            System.out.println(String.format("+------------+----------+------------+------------------+------------+"));
            System.out.println(String.format("| Term       | Credits  | Course ID  | Course Name      | Grade      |"));
            System.out.println(String.format("+------------+----------+------------+------------------+------------+"));

            while (rs.next()) {
                System.out.println(String.format("| %-10s | %-8d | %-10d | %-16s | %-10s |",
                        rs.getString("term"), rs.getInt("credits"), rs.getInt("course_id"), rs.getString("course_name"), rs.getString("grade")));
            }	
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private static void registerStudent(Scanner s) throws Exception {
    	String terms = """
    				   SELECT DISTINCT s.term 
    				   FROM Sections s
    				   """;
        System.out.println("Available terms:");
        try (PreparedStatement termsSt = conn.prepareStatement(terms)){        	
        	ResultSet rs = termsSt.executeQuery();        	
        	while (rs.next()) {
        		System.out.println(rs.getString("term"));
        	} 
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        	System.out.print("Choose a term: ");
        	String chosenTerm = s.nextLine();

        	String sections = """
        				  	  SELECT r.section_id, c.course_id, c.course_name, c.section_cap, s.seats_available
        			      	  FROM Courses c
        				  	  JOIN Sections s ON c.course_id = s.course_id
        				  	  JOIN Registrations r ON s.section_id = r.section_id
        				  	  WHERE s.term = ?
        				  	  """;
        	System.out.println("Available sections for term " + chosenTerm + ":");
        	try (PreparedStatement sectionsSt = conn.prepareStatement(sections)) {
        		sectionsSt.setString(1, chosenTerm);
        		ResultSet rs = sectionsSt.executeQuery();     
        		/**
        		 * GENERATIVE AI USED FOR TABLE FORMATTING:
        		 * System.out.println(String.format("Section ID = %d Course ID = %d Course Name = %s Cap = %d Available = %d",
        				rs.getInt("section_id"), rs.getInt("course_id"), rs.getString("course_name"), rs.getInt("section_cap"), rs.getInt("seats_available"))
        				);

        				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
        		 */
        		System.out.println(String.format("+------------+-----------+------------------+--------+--------------+"));
        		System.out.println(String.format("| Section ID | Course ID | Course Name      | Cap    | Available    |"));
        		System.out.println(String.format("+------------+-----------+------------------+--------+--------------+"));

        		while (rs.next()) {
        		    System.out.println(String.format("| %-10d | %-9d | %-16s | %-6d | %-12d |",
        		            rs.getInt("section_id"), rs.getInt("course_id"), rs.getString("course_name"),
        		            rs.getInt("section_cap"), rs.getInt("seats_available")));
        		}

        		System.out.println("+------------+-----------+------------------+--------+--------------+");

        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        	
        

        System.out.print("Section ID to register for: ");
        int sectionId = Integer.parseInt(s.nextLine());
        
        String studentList = """
        		      		 SELECT DISTINCT r.student_id, p.last_name, p.first_name
        		      		 FROM People p
        		      		 JOIN Registrations r ON p.person_id = r.student_id
        		      		 JOIN People_Roles pr ON p.person_id = pr.person_id
        		      		 WHERE pr.role_id = 100
        					 """;
            System.out.println("Students available for registration:");
            try (PreparedStatement studentSt = conn.prepareStatement(studentList);
                 ResultSet rs = studentSt.executeQuery()) {
            	/**
        		 * GENERATIVE AI USED FOR TABLE FORMATTING:
        		 * System.out.println(String.format("Student ID=%d Last Name=%s First Name=%s",
            				rs.getInt("student_id"), rs.getString("last_name"), rs.getString("first_name")
            		));

        				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
        		 */
            	System.out.println(String.format("+------------+--------------+-------------+"));
                System.out.println(String.format("| Student ID | Last Name    | First Name  |"));
                System.out.println(String.format("+------------+--------------+-------------+"));

                while (rs.next()) {
                    System.out.println(String.format("| %-10d | %-12s | %-11s |",
                            rs.getInt("student_id"), rs.getString("last_name"), rs.getString("first_name")
                    ));
                }
                System.out.println("+------------+--------------+-------------+");
            } catch (Exception e) {
            	e.printStackTrace();
            }
            
            
        
        System.out.print("Enter your Student ID: ");
        int studentId = Integer.parseInt(s.nextLine());

        String seatCheck = """
        				   SELECT s.seats_available 
        				   FROM Sections s
        				   WHERE s.section_id = ?
        				   """;
        try (PreparedStatement checkStmt = conn.prepareStatement(seatCheck)) {
            checkStmt.setInt(1, sectionId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int seats = rs.getInt("seats_available");
                if (seats <= 0) {
                    System.out.println("No seats available.");
                    return;
                }
            } else {
                System.out.println("Section not found.");
                return;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }

        String enroll = """
        				INSERT INTO Registrations (section_id, student_id)
        				VALUES (?, ?)       		
        				""";
        try (PreparedStatement enrollSt = conn.prepareStatement(enroll)) {
        	enrollSt.setInt(1, sectionId);
        	enrollSt.setInt(2, studentId);
        	enrollSt.executeUpdate();
        } catch (Exception e) {
        	e.printStackTrace();
        }

        String updateSeats = """
        					 UPDATE sections 
        					 SET seats_available = seats_available - 1 
        					 WHERE section_id = ?
        					 """;
        try (PreparedStatement seatSt = conn.prepareStatement(updateSeats)) {
            seatSt.setInt(1, sectionId);
            seatSt.executeUpdate();
        } catch (Exception e) {
        	e.printStackTrace();
        }

        System.out.println("Registered successfully.");
    }

    private static void postGrade(Scanner s) throws Exception {
    	String terms = """
				   	   SELECT DISTINCT s.term 
				       FROM Sections s
				       """;
    	System.out.println("Available terms:");
    	try (PreparedStatement termsSt = conn.prepareStatement(terms);
    			ResultSet rs = termsSt.executeQuery()) {        	
    		while (rs.next()) {
    			System.out.println(rs.getString("term"));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	System.out.print("Choose a term: ");
    	String chosenTerm = s.nextLine();

    	String sections = """
    				  	  SELECT r.section_id, c.course_id, c.course_name, c.section_cap, s.seats_available
    			      	  FROM Courses c
    				  	  JOIN Sections s ON c.course_id = s.course_id
    				  	  JOIN Registrations r ON s.section_id = r.section_id
    				  	  WHERE s.term = ?
    				  	  """;
    	System.out.println("Available sections for term " + chosenTerm + ":");
    	try (PreparedStatement sectionsSt = conn.prepareStatement(sections)) {
    		sectionsSt.setString(1, chosenTerm);
    		ResultSet rs = sectionsSt.executeQuery();   
    		/**
    		 * GENERATIVE AI USED FOR TABLE FORMATTING:
    		 * System.out.println(String.format("Section ID=%d Course ID=%d Course Name=%s Cap=%d Available = %d",
    				rs.getInt("section_id"), rs.getInt("course_id"), rs.getString("course_name"), rs.getInt("section_cap"), rs.getInt("seats_available"))
    				);

    				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
    		 */    		
    		    System.out.println(String.format("+------------+------------+-----------------+-----+-----------------+"));
    		    System.out.println(String.format("| Section ID | Course ID | Course Name     | Cap | Available       |"));
    		    System.out.println(String.format("+------------+------------+-----------------+-----+-----------------+"));
    		    while (rs.next()) {
    		    	System.out.println(String.format("| %-10d | %-10d | %-15s | %-3d | %-15d |",
    		            rs.getInt("section_id"), rs.getInt("course_id"), rs.getString("course_name"),
    		            rs.getInt("section_cap"), rs.getInt("seats_available")
    		    ));
    		    System.out.println("+------------+------------+-----------------+-----+-----------------+");
    		}

    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	System.out.print("Choose a Section ID: ");
        int sectionId = Integer.parseInt(s.nextLine());
        
        String studentList = """
        		      		 SELECT DISTINCT r.student_id, r.grade
        		      		 FROM People p
        		      		 JOIN Registrations r ON p.person_id = r.student_id
        		      		 JOIN Sections s ON r.section_id = s.section_id
        		      		 JOIN People_Roles pr ON p.person_id = pr.person_id
        		      		 WHERE pr.role_id = 100
        		      		 AND s.section_id = ?
        						""";
        System.out.println("Students in section "+sectionId+":");
        try (PreparedStatement studentSt = conn.prepareStatement(studentList)){
        	studentSt.setInt(1, sectionId);
            ResultSet rs = studentSt.executeQuery();
            /**
    		 * GENERATIVE AI USED FOR TABLE FORMATTING:
    		 * System.out.println(String.format("Student ID=%d Grade=%s",
            		rs.getInt("student_id"), rs.getString("grade")
            	));

    				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
    		 */
            System.out.println(String.format("+------------+-------+"));
            System.out.println(String.format("| Student ID | Grade |"));
            System.out.println(String.format("+------------+-------+"));
            while (rs.next()) {
                System.out.println(String.format("| %-10d | %-5s |",
                        rs.getInt("student_id"), rs.getString("grade")
                ));
            } 
            System.out.println("+------------+-------+");
        } catch (Exception e) {
        	e.printStackTrace();
        }
            
        System.out.print("Enter a Student's ID: ");
        int studentId = Integer.parseInt(s.nextLine());
    	
        String facultyList = """
        		   		  	 SELECT DISTINCT p.person_id, p.last_name, p.first_name
        		  			 FROM People p
        		      		 JOIN People_Roles pr ON p.person_id = pr.person_id
        		  			 WHERE pr.role_id = 101
							 """;
        System.out.println("List of faculty members:");
        try (PreparedStatement facultySt = conn.prepareStatement(facultyList)){
            ResultSet rs = facultySt.executeQuery();
            /**
    		 * GENERATIVE AI USED FOR TABLE FORMATTING:
    		 * System.out.println(String.format("Person ID=%d Last=%s First=%s",
            			rs.getInt("person_id"), rs.getString("last_name"), rs.getString("first_name")
            	));

    				); ORIGINAL FORMAT LISTED ABOVE, CHANGED FOR ALL PRINT STATEMENTS
    		 */
            System.out.println(String.format("+-----------+------------+-----------+"));
            System.out.println(String.format("| Person ID | Last Name | First Name |"));
            System.out.println(String.format("+-----------+------------+-----------+"));
            while (rs.next()) {
                System.out.println(String.format("| %-9d | %-10s | %-9s |",
                        rs.getInt("person_id"), rs.getString("last_name"), rs.getString("first_name")
                ));
            } 
            System.out.println("+-----------+------------+-----------+");
        } catch (Exception e) {
            e.printStackTrace();
        }	    
        System.out.print("Enter your faculty ID (person ID): ");
        int facultyId = Integer.parseInt(s.nextLine());    
            
        System.out.print("Enter a grade: ");
        String grade = s.nextLine().toUpperCase();
        
        String findGrade = """
        	    		   SELECT COUNT(*)
        	               FROM Registrations r
        	               WHERE r.section_id = ? AND r.student_id = ?
        				   """;

        PreparedStatement findSt = conn.prepareStatement(findGrade);
        findSt.setInt(1, sectionId);
        findSt.setInt(2, studentId);
        ResultSet rs = findSt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
        	String updateGrade = """
        	                     UPDATE Registrations 
        	                     SET grade = ?, grade_posted_on = CURRENT_DATE, grade_posted_by = ?
        	                     WHERE section_id = ? AND student_id = ?
        	                     """;
        	try (PreparedStatement updateSt = conn.prepareStatement(updateGrade)) {
        		updateSt.setString(1, grade);
        	    updateSt.setInt(2, facultyId);
        	    updateSt.setInt(3, sectionId);
        	    updateSt.setInt(4, studentId);
        	    updateSt.executeUpdate();
        	    System.out.println("Grade updated.");
        	} catch (Exception e) {
        		e.printStackTrace();
        	  }
        }

        else { 
        	String enterGrade = """
        					 	INSERT INTO registrations (section_id, student_id, grade, grade_posted_on, grade_posted_by) 
        			            VALUES (?, ?, ?, CURRENT_DATE, ?)                     
        					    """;
        	
        	try (PreparedStatement gradeSt = conn.prepareStatement(enterGrade)) {
            	gradeSt.setInt(1, sectionId);
            	gradeSt.setInt(2, studentId);
            	gradeSt.setString(3, grade);
            	gradeSt.setInt(4, facultyId);
            	gradeSt.executeUpdate();
        	} catch (Exception e) {
        		e.printStackTrace();
        	}

        	System.out.println("Grade posted.");
        }
    }
}
