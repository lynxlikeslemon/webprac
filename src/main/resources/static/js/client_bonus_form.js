function addRemoveButtonListener(button) {
    button.addEventListener('click', function() {
        this.closest('.bonus-card-item').remove();
    });
}

document.querySelectorAll('.remove-card').forEach(button => {
    addRemoveButtonListener(button);
});

document.getElementById('addBonusCard').addEventListener('click', function() {
    const container = document.getElementById('bonusCardContainer');
    const template = document.getElementById('bonusCardTemplate');
    const newCard = template.cloneNode(true);
    newCard.id = '';
    newCard.querySelectorAll('input').forEach(input => {
        if (input.type !== 'button') {
            input.value = '';
        }
    });
    container.appendChild(newCard);

    const removeBtn = newCard.querySelector('.remove-card');
    addRemoveButtonListener(removeBtn);
});