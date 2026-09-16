(function () {
    function initTableFilters() {
        window.AppDom.bySelector("[data-filter-input]").forEach(function (input) {
            input.addEventListener("input", function () {
                var targetName = input.getAttribute("data-filter-target");
                var table = document.querySelector('[data-filter-table="' + targetName + '"]');
                if (!table) {
                    return;
                }

                var query = input.value.trim().toLowerCase();
                window.AppDom.bySelector("tbody tr", table).forEach(function (row) {
                    var text = row.textContent.toLowerCase();
                    row.classList.toggle("is-hidden", query.length > 0 && text.indexOf(query) === -1);
                });
            });
        });
    }

    window.AppDom.onReady(initTableFilters);
})();
