(function () {
    function initAlerts() {
        window.AppDom.bySelector("[data-dismiss-alert]").forEach(function (button) {
            button.addEventListener("click", function () {
                var alert = button.closest("[data-alert]");
                if (alert) {
                    alert.remove();
                }
            });
        });

        window.setTimeout(function () {
            window.AppDom.bySelector("[data-alert]").forEach(function (alert) {
                if (!alert.matches(":hover")) {
                    alert.remove();
                }
            });
        }, 7000);
    }

    window.AppDom.onReady(initAlerts);
})();
