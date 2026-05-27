document.addEventListener('DOMContentLoaded', function () {
    const tabs = document.querySelectorAll('.tab');
    const contents = document.querySelectorAll('.tab-content');

    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (item) {
                item.classList.remove('active');
            });

            contents.forEach(function (content) {
                content.classList.remove('active');
            });

            tab.classList.add('active');

            const targetId = tab.dataset.tab;
            const target = document.getElementById(targetId);

            if (target) {
                target.classList.add('active');
            }
        });
    });
});