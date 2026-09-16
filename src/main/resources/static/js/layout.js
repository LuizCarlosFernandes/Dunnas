(function () {
    function initSidebarToggle() {
        var sidebar = document.querySelector("[data-sidebar]");
        var toggle = document.querySelector("[data-sidebar-toggle]");
        var backdrop = document.querySelector("[data-sidebar-backdrop]");
        if (!sidebar || !toggle) {
            return;
        }

        function setSidebarOpen(isOpen) {
            sidebar.classList.toggle("is-open", isOpen);
            if (backdrop) {
                backdrop.classList.toggle("is-visible", isOpen);
            }
        }

        toggle.addEventListener("click", function () {
            var willOpen = !sidebar.classList.contains("is-open");
            setSidebarOpen(willOpen);
        });

        if (backdrop) {
            backdrop.addEventListener("click", function () {
                setSidebarOpen(false);
            });
        }

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                setSidebarOpen(false);
            }
        });

        document.addEventListener("click", function (event) {
            if (window.innerWidth > 980) {
                return;
            }

            if (!sidebar.contains(event.target) && !toggle.contains(event.target)) {
                setSidebarOpen(false);
            }
        });
    }

    function initActiveNav() {
        var links = document.querySelectorAll(".nav-link");
        if (!links.length) {
            return;
        }

        var currentPath = window.location.pathname;
        var bestMatch = null;
        var bestLength = -1;

        links.forEach(function (link) {
            var targetPath = new URL(link.href, window.location.origin).pathname;
            var isMatch = currentPath === targetPath || currentPath.indexOf(targetPath + "/") === 0;

            if (isMatch && targetPath.length > bestLength) {
                bestMatch = link;
                bestLength = targetPath.length;
            }
        });

        if (bestMatch) {
            bestMatch.classList.add("is-active");
            bestMatch.setAttribute("aria-current", "page");
        }
    }

    function init() {
        initSidebarToggle();
        initActiveNav();
    }

    window.AppDom.onReady(init);
})();
