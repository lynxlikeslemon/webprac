function addRemoveButtonListener(button) {
    button.addEventListener('click', function() {
        this.closest('.bonus-card-item').remove();
    });
}

document.querySelectorAll('.remove-card').forEach(button => {
    addRemoveButtonListener(button);
});

let i = 0;

document.getElementById('addBonusCard').addEventListener('click', function() {
    const container = document.getElementById('bonusCardContainer');
    const template = document.getElementById('bonusCardTemplate');
    const newCard = template.cloneNode(true);
    newCard.style.display = 'block';
    newCard.id = 'bonusCard' + i;
    i++;
    container.appendChild(newCard);

    const removeBtn = newCard.querySelector('.remove-card');
    addRemoveButtonListener(removeBtn);
});