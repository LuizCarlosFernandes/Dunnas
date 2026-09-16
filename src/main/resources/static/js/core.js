(function () {
    function bySelector(selector, scope) {
        return Array.from((scope || document).querySelectorAll(selector));
    }

    function onReady(callback) {
        document.addEventListener("DOMContentLoaded", callback);
    }

    window.AppDom = {
        bySelector: bySelector,
        onReady: onReady
    };
})();
