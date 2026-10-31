/**
 * Examination Navigation and Interactive MCQ Selector
 */
document.addEventListener('DOMContentLoaded', () => {
    const questionCards = document.querySelectorAll('.question-card-item');
    if (!questionCards.length) return;

    const totalQuestions = questionCards.length;
    let currentIndex = 0;

    const prevBtn = document.getElementById('prevQuestionBtn');
    const nextBtn = document.getElementById('nextQuestionBtn');
    const clearBtn = document.getElementById('clearAnswerBtn');
    const paletteButtons = document.querySelectorAll('.palette-btn');
    const answeredCountDisplay = document.getElementById('answeredCountDisplay');
    const modalAnsweredCount = document.getElementById('modalAnsweredCount');
    const progressBar = document.getElementById('examProgressBar');

    function showQuestion(index) {
        if (index < 0 || index >= totalQuestions) return;
        currentIndex = index;

        // Hide all cards, show active
        questionCards.forEach((card, idx) => {
            if (idx === index) {
                card.classList.remove('d-none');
            } else {
                card.classList.add('d-none');
            }
        });

        // Update Prev / Next buttons
        if (prevBtn) {
            prevBtn.disabled = (currentIndex === 0);
        }

        if (nextBtn) {
            if (currentIndex === totalQuestions - 1) {
                nextBtn.innerHTML = 'Review & Submit <i class="bi bi-arrow-right"></i>';
                nextBtn.classList.remove('btn-primary-purple');
                nextBtn.classList.add('btn-soft-mint');
            } else {
                nextBtn.innerHTML = 'Next Question <i class="bi bi-chevron-right"></i>';
                nextBtn.classList.add('btn-primary-purple');
                nextBtn.classList.remove('btn-soft-mint');
            }
        }

        // Highlight current button in side palette
        paletteButtons.forEach((btn, idx) => {
            if (idx === currentIndex) {
                btn.classList.add('palette-current');
            } else {
                btn.classList.remove('palette-current');
            }
        });
    }

    function updateAnsweredCount() {
        let answered = 0;

        questionCards.forEach((card, idx) => {
            const checkedRadio = card.querySelector('input[type="radio"]:checked');
            const paletteBtn = document.querySelector(`.palette-btn[data-index="${idx}"]`);

            if (checkedRadio) {
                answered++;
                if (paletteBtn) paletteBtn.classList.add('palette-answered');
            } else {
                if (paletteBtn) paletteBtn.classList.remove('palette-answered');
            }
        });

        if (answeredCountDisplay) answeredCountDisplay.textContent = answered;
        if (modalAnsweredCount) modalAnsweredCount.textContent = answered;

        if (progressBar) {
            const pct = Math.round((answered / totalQuestions) * 100);
            progressBar.style.width = pct + '%';
            progressBar.setAttribute('aria-valuenow', pct);
        }
    }

    // Option selection click handler
    document.querySelectorAll('.option-item').forEach(item => {
        item.addEventListener('click', function () {
            const radio = this.querySelector('input[type="radio"]');
            if (radio) {
                radio.checked = true;
                // Update active class on siblings
                const parentList = this.closest('.mcq-options-list');
                parentList.querySelectorAll('.option-item').forEach(opt => opt.classList.remove('selected'));
                this.classList.add('selected');
                updateAnsweredCount();
            }
        });
    });

    // Clear Answer button
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            const currentCard = questionCards[currentIndex];
            const checkedRadio = currentCard.querySelector('input[type="radio"]:checked');
            if (checkedRadio) {
                checkedRadio.checked = false;
                currentCard.querySelectorAll('.option-item').forEach(opt => opt.classList.remove('selected'));
                updateAnsweredCount();
            }
        });
    }

    // Next / Previous buttons
    if (nextBtn) {
        nextBtn.addEventListener('click', () => {
            if (currentIndex < totalQuestions - 1) {
                showQuestion(currentIndex + 1);
            } else {
                // Last question: trigger submit confirmation modal
                const submitModalEl = document.getElementById('submitConfirmModal');
                if (submitModalEl) {
                    const modal = new bootstrap.Modal(submitModalEl);
                    modal.show();
                }
            }
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', () => {
            if (currentIndex > 0) {
                showQuestion(currentIndex - 1);
            }
        });
    }

    // Palette direct question jump
    paletteButtons.forEach(btn => {
        btn.addEventListener('click', function () {
            const targetIdx = parseInt(this.getAttribute('data-index'));
            showQuestion(targetIdx);
        });
    });

    // Initialize first view & count
    showQuestion(0);
    updateAnsweredCount();
});
