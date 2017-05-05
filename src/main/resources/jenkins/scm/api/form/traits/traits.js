(function () {
    var traitSectionRule = function (e) {
        e = $(e);
        var p = $(e.previousSibling);
        var n = $(e.nextSibling);
        var empty = (!n || n.hasClassName("trait-section") || n.hasClassName("repeatable-insertion-point"));
        // find any previous entries
        while (p && p.hasClassName("trait-section")) {
            p = $(p.previousSibling);
        }
        if (!empty) {
            // skip our entries
            while (n && !n.hasClassName("trait-section") && !n.hasClassName("repeatable-insertion-point")) {
                n = $(n.nextSibling);
            }
        }
        // find next section entries
        while (n && n.hasClassName("trait-section") && !n.hasClassName("repeatable-insertion-point")) {
            n = $(n.nextSibling);
        }
        if ((!p && n.hasClassName("repeatable-insertion-point")) || empty) {
            e.addClassName("trait-section-empty");
        } else {
            e.removeClassName("trait-section-empty");
        }
    };
    Behaviour.specify("DIV.trait-container", 'traits', -50, function (e) {
        e = $(e);
        if (isInsideRemovable(e)) {
            return;
        }
        layoutUpdateCallback.add(function () {
            findElementsBySelector(e, ".trait-section").each(traitSectionRule);
        })
    });
    Behaviour.specify(".repeatable-delete", 'traits', 500, function (e) {
        var c = findAncestorClass(e, "trait-container");
        if (c) {
            var btn = YAHOO.widget.Button.getButton(e.id);
            if (btn) {
                btn.on("click", function () {
                    window.setTimeout(function () {
                        findElementsBySelector(c, ".trait-section").each(traitSectionRule);
                    }, 250);
                });
            }
        }
    });
})();

