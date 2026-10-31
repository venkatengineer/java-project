/**
 * Examination Countdown Timer
 */
function initExamTimer(durationMinutes, examId, submitFormId) {
    const timerDisplay = document.getElementById('timerDisplay');
    const timerBox = document.getElementById('timerBox');
    const examForm = document.getElementById(submitFormId || 'examForm');

    if (!timerDisplay || !examForm) return;

    const storageKey = `exam_end_time_${examId}`;
    let endTime = localStorage.getItem(storageKey);

    const now = new Date().getTime();
    const durationMs = durationMinutes * 60 * 1000;

    if (!endTime || parseInt(endTime) <= now) {
        endTime = now + durationMs;
        localStorage.setItem(storageKey, endTime);
    } else {
        endTime = parseInt(endTime);
    }

    function updateTimer() {
        const currentTime = new Date().getTime();
        const remainingMs = endTime - currentTime;

        if (remainingMs <= 0) {
            clearInterval(timerInterval);
            timerDisplay.textContent = "00:00:00";
            localStorage.removeItem(storageKey);

            // Notify student and automatically submit
            alert("Examination time has expired! Your answers are being submitted automatically.");
            examForm.submit();
            return;
        }

        const hours = Math.floor(remainingMs / (1000 * 60 * 60));
        const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60));
        const seconds = Math.floor((remainingMs % (1000 * 60)) / 1000);

        const formatted =
            (hours > 0 ? String(hours).padStart(2, '0') + ':' : '') +
            String(minutes).padStart(2, '0') + ':' +
            String(seconds).padStart(2, '0');

        timerDisplay.textContent = formatted;

        // Visual alert when less than 5 minutes remaining
        if (remainingMs < 5 * 60 * 1000) {
            timerBox.classList.add('timer-warning');
        } else {
            timerBox.classList.remove('timer-warning');
        }
    }

    updateTimer();
    const timerInterval = setInterval(updateTimer, 1000);

    // Clean up storage when submitting normally
    examForm.addEventListener('submit', () => {
        localStorage.removeItem(storageKey);
    });
}
