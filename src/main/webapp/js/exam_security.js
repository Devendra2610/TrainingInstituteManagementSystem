// Proctoring & Audit component to detect tab switching/blur events

document.addEventListener("DOMContentLoaded", function() {
    
    function logSuspiciousActivity(eventType) {
        if (!examId || !contextPath) return; // Ensure variables are set in JSP
        
        const xhr = new XMLHttpRequest();
        xhr.open("POST", contextPath + "/exam/security", true);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhr.send("event=" + encodeURIComponent(eventType) + "&examId=" + encodeURIComponent(examId));
        
        console.warn("Suspicious activity detected: " + eventType);
        // Optional: show a warning to the user
        alert("Warning: " + eventType + " detected. This activity has been logged.");
    }

    // Detect Tab Switching or minimizing
    document.addEventListener("visibilitychange", function() {
        if (document.visibilityState === 'hidden') {
            logSuspiciousActivity("TAB_SWITCH_OR_MINIMIZE");
        }
    });

    // Detect Window Blur (losing focus)
    window.addEventListener("blur", function() {
        logSuspiciousActivity("WINDOW_BLUR_LOST_FOCUS");
    });
    
});
