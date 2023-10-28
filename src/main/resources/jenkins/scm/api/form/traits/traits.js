(function () {
    var traitSectionRule = function (e) {
        var p = e.previousSibling;
        var n = e.nextSibling;
        var empty = (!n || n.classList.contains("trait-section") || n.classList.contains("repeatable-insertion-point"));
        // find any previous entries
        while (p && p.classList.contains("trait-section")) {
            p = p.previousSibling;
        }
        if (!empty) {
            // skip our entries
            while (n && !n.classList.contains("trait-section") && !n.classList.contains("repeatable-insertion-point")) {
                n = n.nextSibling;
            }
        }
        // find next section entries
        while (n && n.classList.contains("trait-section") && !n.classList.contains("repeatable-insertion-point")) {
            n = n.nextSibling;
        }
        if ((!p && n.classList.contains("repeatable-insertion-point")) || empty) {
            e.classList.add("trait-section-empty");
        } else {
            e.classList.remove("trait-section-empty");
        }
    };
    Behaviour.specify("DIV.trait-container", 'traits', -50, function (e) {
        if (isInsideRemovable(e)) {
            return;
        }
        layoutUpdateCallback.add(function () {
            e.querySelectorAll(".trait-section").forEach(traitSectionRule);
        })
    });
    Behaviour.specify(".repeatable-delete", 'traits', 500, function (e) {
        var c = e.closest(".trait-container");
        if (c) {
            var btn = YAHOO.widget.Button.getButton(e.id);
            if (btn) {
                btn.on("click", function () {
                    window.setTimeout(function () {
                        c.querySelectorAll(".trait-section").forEach(traitSectionRule);
                    }, 250);
                });
            }
        }
    });
})();
