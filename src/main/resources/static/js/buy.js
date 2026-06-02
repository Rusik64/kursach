let selectedProducts = [];

function addProduct() {
    const select = document.getElementById('productList');
    const countInput = document.getElementById('productCount');
    const selectedOption = select.options[select.selectedIndex];

    if (!selectedOption || selectedOption.value === '') {
        alert('Выберите товар');
        return;
    }

    const count = parseInt(countInput.value);
    if (!count || count < 1) {
        alert('Введите корректное количество');
        return;
    }

    // Получаем данные из data-атрибутов
    const productId = selectedOption.value;
    const productName = selectedOption.getAttribute('data-name');
    const price = parseFloat(selectedOption.getAttribute('data-price'));
    const maxStock = parseInt(selectedOption.getAttribute('data-stock'));

    console.log('Adding product:', { productId, productName, price, maxStock, count });

    if (count > maxStock) {
        alert('Недостаточно товара на складе. Доступно: ' + maxStock);
        return;
    }

    // Проверяем, есть ли уже такой товар в чеке
    const existingIndex = selectedProducts.findIndex(p => p.productId == productId);
    if (existingIndex >= 0) {
        if (selectedProducts[existingIndex].count + count > maxStock) {
            alert('Недостаточно товара на складе. Доступно: ' + maxStock);
            return;
        }
        selectedProducts[existingIndex].count += count;
    } else {
        selectedProducts.push({
            productId: parseInt(productId),
            name: productName,
            price: price,
            count: count
        });
    }

    countInput.value = '1';
    updateBuyTable();
}

function removeProduct(index) {
    selectedProducts.splice(index, 1);
    updateBuyTable();
}

function updateBuyTable() {
    const tbody = document.querySelector('#buyItems tbody');
    tbody.innerHTML = '';

    let totalSum = 0;

    selectedProducts.forEach((product, index) => {
        const itemSum = product.price * product.count;
        totalSum += itemSum;

        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${product.name || 'Без названия'}</td>
            <td>${product.price.toFixed(2)} руб.</td>
            <td>
                <input type="number" value="${product.count}" min="1"
                       onchange="updateProductCount(${index}, this.value)" class="count-input">
            </td>
            <td>${itemSum.toFixed(2)} руб.</td>
            <td><button onclick="removeProduct(${index})" class="remove-btn">Удалить</button></td>
        `;
    });

    document.getElementById('totalSum').textContent = totalSum.toFixed(2);
}

function updateProductCount(index, newCount) {
    const count = parseInt(newCount);
    if (!count || count < 1) {
        alert('Введите корректное количество');
        updateBuyTable();
        return;
    }

    selectedProducts[index].count = count;
    updateBuyTable();
}

function submitBuy() {
    if (selectedProducts.length === 0) {
        alert('Добавьте товары в чек');
        return;
    }

    const buyData = selectedProducts.map(p => ({
        productId: p.productId,
        count: p.count
    }));

    console.log('Submitting buy:', buyData);

    fetch('/buys/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(buyData)
    })
    .then(response => response.text())
    .then(result => {
        if (result === 'success') {
            alert('Чек успешно создан!');
            window.location.reload();
        } else {
            alert('Ошибка: ' + result);
        }
    })
    .catch(error => {
        alert('Ошибка при создании чека');
        console.error('Error:', error);
    });
}

// Поиск товаров
document.getElementById('productSearch')?.addEventListener('input', function(e) {
    const searchText = e.target.value.toLowerCase();
    const options = document.getElementById('productList').options;

    for (let option of options) {
        const text = option.text.toLowerCase();
        option.style.display = text.includes(searchText) ? '' : 'none';
    }
});

// Обработка Enter в поле количества
document.getElementById('productCount')?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        addProduct();
    }
});

// Двойной клик по товару для быстрого добавления
document.getElementById('productList')?.addEventListener('dblclick', function() {
    addProduct();
});