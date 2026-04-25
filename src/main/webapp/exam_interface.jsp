<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.institute.dao.ExamDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    // Ensure session exists and exam attributes are set
    if (session.getAttribute("userId") == null || session.getAttribute("examId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    
    // Server-side initialization of timer
    Long startTime = (Long) session.getAttribute("examStartTime");
    if (startTime == null) {
        startTime = System.currentTimeMillis();
        session.setAttribute("examStartTime", startTime);
    }
    
    Integer durationMinutes = (Integer) session.getAttribute("examDuration");
    if (durationMinutes == null) durationMinutes = 60; // Default 60 mins
    
    long timeElapsed = System.currentTimeMillis() - startTime;
    long timeLeftMillis = (durationMinutes * 60 * 1000L) - timeElapsed;
    if (timeLeftMillis < 0) timeLeftMillis = 0;

    int examId = (Integer) session.getAttribute("examId");
    ExamDAO examDAO = new ExamDAO();
    List<Map<String, Object>> questions = examDAO.getQuestions(examId);
    
    // Fetch saved answers if any
    Map<Integer, String> savedAnswers = (Map<Integer, String>) session.getAttribute("savedAnswers");
    if (savedAnswers == null) savedAnswers = new java.util.HashMap<>();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Online Examination</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f1f5f9; padding: 20px; }
        .exam-container { max-width: 900px; margin: auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
        .header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #e2e8f0; padding-bottom: 15px; margin-bottom: 20px; }
        .timer { font-size: 24px; font-weight: bold; color: #ef4444; background: #fee2e2; padding: 8px 15px; border-radius: 6px; }
        .question-nav { display: flex; gap: 10px; margin-bottom: 20px; flex-wrap: wrap; }
        .q-btn { width: 40px; height: 40px; border-radius: 50%; border: none; background: #e2e8f0; font-weight: bold; cursor: pointer; font-size: 16px; }
        .q-btn.active { background: #3b82f6; color: white; border: 2px solid #1d4ed8; }
        .q-btn.answered { background: #10b981; color: white; }
        .q-btn.review { background: #f59e0b; color: white; }
        
        .question-block { display: none; margin-top: 20px; min-height: 250px; }
        .question-block.active { display: block; }
        .question-text { font-size: 1.2rem; font-weight: 500; margin-bottom: 20px; color: #1e293b; }
        
        .options label { display: block; margin: 15px 0; padding: 10px 15px; border: 1px solid #cbd5e1; border-radius: 6px; cursor: pointer; transition: 0.2s; }
        .options label:hover { background-color: #f8fafc; }
        .options input[type="radio"] { margin-right: 15px; transform: scale(1.2); }
        
        .controls { display: flex; justify-content: space-between; margin-top: 40px; padding-top: 20px; border-top: 1px solid #e2e8f0; }
        .btn { padding: 10px 20px; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 16px; }
        .btn-nav { background: #64748b; color: white; }
        .btn-review { background: #f59e0b; color: white; }
        .btn-submit { background: #ef4444; color: white; }
    </style>
</head>
<body>
    <div class="exam-container">
        <div class="header">
            <h2>Exam ID: <%= examId %></h2>
            <div class="timer" id="timerDisplay">Loading...</div>
        </div>
        
        <div class="question-nav" id="questionNav">
            <% for(int i=0; i<questions.size(); i++) { %>
                <button type="button" class="q-btn" id="nav-<%=i%>" onclick="goToQuestion(<%=i%>)"><%= (i+1) %></button>
            <% } %>
        </div>
        
        <form id="examForm" action="<%= request.getContextPath() %>/exam/controller" method="post">
            <input type="hidden" name="action" value="submit">
            <input type="hidden" name="examId" value="<%= examId %>">
            
            <% for(int i=0; i<questions.size(); i++) { 
                Map<String, Object> q = questions.get(i);
                int qId = (Integer) q.get("question_id");
                String savedAns = savedAnswers.get(qId);
            %>
            <div class="question-block <%= i == 0 ? "active" : "" %>" id="q-<%=i%>">
                <div class="question-text">Q<%= (i+1) %>. <%= q.get("question_text") %></div>
                
                <% if("MCQ".equals(q.get("question_type"))) { %>
                    <div class="options">
                        <label><input type="radio" name="q_<%=qId%>" value="A" <%= "A".equals(savedAns) ? "checked" : "" %> onchange="answerSelected(<%=i%>, <%=qId%>, 'A')"> A) <%= q.get("option_a") %></label>
                        <label><input type="radio" name="q_<%=qId%>" value="B" <%= "B".equals(savedAns) ? "checked" : "" %> onchange="answerSelected(<%=i%>, <%=qId%>, 'B')"> B) <%= q.get("option_b") %></label>
                        <label><input type="radio" name="q_<%=qId%>" value="C" <%= "C".equals(savedAns) ? "checked" : "" %> onchange="answerSelected(<%=i%>, <%=qId%>, 'C')"> C) <%= q.get("option_c") %></label>
                        <label><input type="radio" name="q_<%=qId%>" value="D" <%= "D".equals(savedAns) ? "checked" : "" %> onchange="answerSelected(<%=i%>, <%=qId%>, 'D')"> D) <%= q.get("option_d") %></label>
                    </div>
                <% } else { %>
                    <textarea name="q_<%=qId%>" rows="8" style="width:100%; padding:10px; border-radius:6px; border:1px solid #cbd5e1; font-family:inherit;" placeholder="Type your answer here..." onblur="answerSelected(<%=i%>, <%=qId%>, this.value)"><%= savedAns != null ? savedAns : "" %></textarea>
                <% } %>
            </div>
            <% } %>
            
            <div class="controls">
                <div>
                    <button type="button" class="btn btn-nav" onclick="prevQuestion()" id="btnPrev" disabled>Previous</button>
                    <button type="button" class="btn btn-nav" onclick="nextQuestion()" id="btnNext">Next</button>
                </div>
                <div>
                    <button type="button" class="btn btn-review" onclick="toggleReview()">Mark for Review</button>
                    <button type="button" class="btn btn-submit" onclick="submitExam()">Final Submit</button>
                </div>
            </div>
        </form>
    </div>

    <script>
        const totalQuestions = <%= questions.size() %>;
        let currentQ = 0;
        const contextPath = "<%= request.getContextPath() %>";
        
        // Navigation Logic
        function goToQuestion(index) {
            document.querySelectorAll('.question-block').forEach(b => b.classList.remove('active'));
            document.getElementById('q-' + index).classList.add('active');
            
            // Update active state in nav map
            document.querySelectorAll('.q-btn').forEach(b => b.classList.remove('active'));
            document.getElementById('nav-' + index).classList.add('active');
            
            currentQ = index;
            
            document.getElementById('btnPrev').disabled = (currentQ === 0);
            document.getElementById('btnNext').disabled = (currentQ === totalQuestions - 1);
        }
        
        function nextQuestion() { if(currentQ < totalQuestions - 1) goToQuestion(currentQ + 1); }
        function prevQuestion() { if(currentQ > 0) goToQuestion(currentQ - 1); }
        
        function toggleReview() {
            const btn = document.getElementById('nav-' + currentQ);
            btn.classList.toggle('review');
        }

        // Auto-save logic using AJAX
        function answerSelected(index, questionId, answer) {
            const navBtn = document.getElementById('nav-' + index);
            if(answer.trim() !== "") navBtn.classList.add('answered');
            else navBtn.classList.remove('answered');
            
            const xhr = new XMLHttpRequest();
            xhr.open("POST", contextPath + "/exam/controller", true);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.send("action=autosave&questionId=" + questionId + "&answer=" + encodeURIComponent(answer));
        }

        function submitExam() {
            if(confirm("Are you sure you want to finally submit your exam? You cannot change your answers after this.")) {
                document.getElementById('examForm').submit();
            }
        }

        // Timer Logic (Server Controlled)
        let timeLeft = <%= Math.floor(timeLeftMillis / 1000) %>;
        
        function updateTimer() {
            if (timeLeft <= 0) {
                document.getElementById('timerDisplay').innerText = "Time's up!";
                alert("Time is up! Your exam will be auto-submitted.");
                document.getElementById('examForm').submit();
                return;
            }
            
            const minutes = Math.floor(timeLeft / 60);
            const seconds = timeLeft % 60;
            document.getElementById('timerDisplay').innerText = 
                "Time Left: " + String(minutes).padStart(2, '0') + ":" + String(seconds).padStart(2, '0');
                
            timeLeft--;
            setTimeout(updateTimer, 1000);
        }

        // Anti-Cheating: Detect Tab Switch
        document.addEventListener("visibilitychange", function() {
            if (document.hidden) {
                const xhr = new XMLHttpRequest();
                xhr.open("POST", contextPath + "/exam/controller", true);
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                xhr.send("action=logCheat");
                alert("WARNING: Tab switching detected! This cheating incident has been logged by the system.");
            }
        });
        
        // Init
        goToQuestion(0);
        updateTimer();
        
        // Pre-mark answered from session
        <% for(int i=0; i<questions.size(); i++) { 
            int qId = (Integer) questions.get(i).get("question_id");
            if(savedAnswers.containsKey(qId)) { %>
                document.getElementById('nav-<%=i%>').classList.add('answered');
        <%  }
        } %>
    </script>
</body>
</html>
