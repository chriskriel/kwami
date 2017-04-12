var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var ConnectionPanel = (function (_super) {
    __extends(ConnectionPanel, _super);
    function ConnectionPanel(id, heading, debug) {
        if (debug === void 0) { debug = false; }
        var _this = _super.call(this, PanelType.Connect, id, heading, true) || this;
        Utils.debug = debug;
        JsonAjaxClient.setDebug(debug);
        var x = Utils.makeDivFromString(ConnectionPanel.htmlStr);
        _this.div2 = x.cloneNode(true);
        _super.prototype.appendChild.call(_this, _this.div2);
        _this.div2.onmousedown = function (ev) {
            _this.div2.parentElement.setAttribute("draggable", "false");
        };
        _this.div2.onmouseup = function (ev) {
            _this.div2.parentElement.setAttribute("draggable", "true");
        };
        var bttn = document.querySelector("#Connect #connectBtn");
        bttn.onclick = function (ev) {
            ev.stopImmediatePropagation();
            _this.setUrl();
            var status = document.querySelector('#connectInputs #status');
            status.value = 'Connecting...';
            status.style.color = 'orange';
            status.style.fontWeight = 'bold';
            JsonAjaxClient.get('tables', ConnectionPanel.setResponse, [status]);
        };
        _this.show();
        return _this;
    }
    ConnectionPanel.prototype.setUrl = function () {
        var input = document.querySelector("#connectInputs #host");
        var host = input.value;
        input = document.querySelector("#connectInputs #port");
        var port = input.value;
        input = document.querySelector("#connectInputs #context");
        var context = input.value;
        input = document.querySelector("#connectInputs #schema");
        var schema = input.value;
        input = document.querySelector("#connectInputs #sql");
        ConnectionPanel.sqlTemplate = input.value;
        JsonAjaxClient.setUrl("http://" + host + ":" + port + "/" + context + "/" + schema + "/");
    };
    ConnectionPanel.setResponse = function (response, objs) {
        Panel.removePanel(PanelType[PanelType.Schema], true);
        var result = response.results[0];
        var status = objs.pop();
        status.style.fontWeight = 'bold';
        var connException = document.getElementById("connException");
        connException.innerHTML = '';
        if (result.resultType == 'RESULTSET') {
            Panel.getPanel(PanelType[PanelType.Connect]).hide();
            SchemaPanel.getInstance(response).show();
            status.value = 'OK';
            status.style.color = 'green';
        }
        else {
            status.value = 'FAILED';
            status.style.color = 'red';
            if (result.resultType == 'EXCEPTION') {
                connException.innerHTML = "Exception: " + result.toString;
            }
        }
    };
    ConnectionPanel.getInstance = function (isStartup) {
        if (isStartup === void 0) { isStartup = false; }
        var x = Panel.getPanel(PanelType[PanelType.Connect]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new ConnectionPanel(PanelType[PanelType.Connect], "Connection Panel");
            Panel.savePanel(x);
        }
        if (isStartup)
            new Menu();
        return x;
    };
    return ConnectionPanel;
}(Panel));
ConnectionPanel.htmlStr = "\n        <div id=\"connectInputs\">\n            <fieldset>\n                <legend>Connection Parameters</legend>\n                <label for=\"host\">Host: </label>\n                <input id=\"host\" type=\"text\" size=\"30\" value=\"localhost\" />\n                <br>\n                <label for=\"port\">Port: </label>\n                <input id=\"port\" type=\"number\" min=\"4096\" max=\"65535\" value=\"18080\" />\n                <br>\n                <label for=\"context\">Context: </label>\n                <input id=\"context\" type=\"text\" size=\"30\" value=\"sqlmx\" />\n                <br>\n                <label for=\"schema\">Schema: </label>\n                <input id=\"schema\" type=\"text\" size=\"30\" value=\"employees\" />\n                <br>\n                <label for=\"sql\">Sampler: </label>\n                <input id=\"sql\" type=\"text\" size=\"30\" value=\"select * from {} limit 10\" />\n                <br>\n                <label for=\"status\">Status: </label>\n                <input id=\"status\" type=\"text\" value=\"Not tried\" disabled=\"disabled\" />\n                <br>\n                <button id=\"connectBtn\" class=\"pnlButton\">Connect</button>\n            </fieldset>\n            <p id=\"connException\"></p>\n        </div>\n    ";
var TableClickContext = (function () {
    function TableClickContext(panelId, li) {
        this.panelId = panelId;
        this.li = li;
    }
    return TableClickContext;
}());
var SchemaPanel = (function (_super) {
    __extends(SchemaPanel, _super);
    function SchemaPanel(id, heading, tables) {
        var _this = _super.call(this, PanelType.Schema, id, JsonAjaxClient.getUrl(), true) || this;
        if (SchemaPanel.htmlTemplate == null)
            SchemaPanel.htmlTemplate = Utils.makeDivFromString(SchemaPanel.htmlStr);
        _this.div2 = SchemaPanel.htmlTemplate.cloneNode(true);
        _this.div2.style.display = 'block';
        _super.prototype.appendChild.call(_this, _this.div2);
        if (tables != null) {
            var result = tables.results[0];
            var ul_1 = document.querySelector('#' + id + ' #tree');
            while (ul_1.firstChild)
                ul_1.removeChild(ul_1.firstChild);
            result.rows.forEach(function (value, index, array) {
                var li = document.createElement("li");
                li.innerHTML = value.values[3] + '=' + value.values[2];
                var attr = document.createAttribute('id');
                attr.value = value.values[2];
                li.attributes.setNamedItem(attr);
                li.classList.add('nsItem');
                li.onclick = function (ev) {
                    JsonAjaxClient.get("tables/" + value.values[2] + "/metaData", SchemaPanel.processTableMetaData, [new TableClickContext(id, li)]);
                };
                ul_1.appendChild(li);
            });
        }
        return _this;
    }
    SchemaPanel.processTableMetaData = function (metaData, ctxObjs) {
        var ctx = ctxObjs.pop();
        if (Utils.debug)
            console.log("clicked: " + ctx.li.getAttribute('id'));
        var result = metaData.results[0];
        var panel = SqlPanel.getInstance();
        var parts = ConnectionPanel.sqlTemplate.split("{}");
        panel.setSql(parts[0] + ctx.li.getAttribute('id') + parts[1]);
        if (result.resultType == 'EXCEPTION') {
            panel.setStatement("Exception: " + result.toString);
            panel.addResults();
        }
        else {
            panel.setStatement(ctx.li.getAttribute('id'));
            panel.addResults(metaData, [3, 5, 6, 10]);
        }
        panel.show();
        Menu.hideAllMenus();
    };
    SchemaPanel.getInstance = function (tables) {
        var x = Panel.getPanel(PanelType[PanelType.Schema]);
        if (x == null) {
            Panel.nextPanelNumber();
            x = new SchemaPanel(PanelType[PanelType.Schema], "Schema Panel", tables);
            Panel.savePanel(x);
        }
        return x;
    };
    return SchemaPanel;
}(Panel));
SchemaPanel.htmlStr = "\n        <div id=\"metaTree\">\n            <ul id=\"tree\" class=\"scrollable\">\n                <li id=\"tanzkw\" class=\"nsItem\">TANZKW</li>\n                <li id=\"tanzm\" class=\"nsItem\">TANZM</li>\n            </ul>\n        </div>\n    ";
SchemaPanel.htmlTemplate = null;
var SqlPanel = (function (_super) {
    __extends(SqlPanel, _super);
    function SqlPanel(id, heading) {
        var _this = _super.call(this, PanelType.Sql, id, heading) || this;
        if (SqlPanel.sqlTemplate == null) {
            SqlPanel.sqlTemplate = Utils.makeDivFromString(SqlPanel.htmlStr);
        }
        _this.div2 = SqlPanel.sqlTemplate.cloneNode(true);
        _this.div2.onmousedown = function (ev) {
            _this.div2.parentElement.setAttribute("draggable", "false");
        };
        _this.div2.onmouseup = function (ev) {
            _this.div2.parentElement.setAttribute("draggable", "true");
        };
        _super.prototype.appendChild.call(_this, _this.div2);
        var selector = "#" + id + " #statement";
        _this.sql = document.querySelector(selector);
        selector = "#" + id + " #clear";
        var bttn = document.querySelector(selector);
        bttn.onclick = function (ev) {
            ev.stopImmediatePropagation();
            _this.sql.value = '';
        };
        selector = "#" + id + " #exec";
        bttn = document.querySelector(selector);
        bttn.onclick = function (ev) {
            ev.stopImmediatePropagation();
            JsonAjaxClient.post("sql?maxRows=-1", SqlPanel.processResults, "sql=" + _this.sql.value, [_this.sql.value]);
        };
        _this.resultsDisplay = new ResultsDisplay(_this, "right-click to copy to SQL");
        _this.resultsDisplay.addValueCallback(function (value) {
            var sqlText = _this.sql.value;
            var cursorPos = _this.sql.selectionStart + value.length;
            sqlText = _this.sql.value.substr(0, _this.sql.selectionStart) + value
                + _this.sql.value.substr(_this.sql.selectionEnd);
            _this.sql.focus();
            _this.sql.value = sqlText;
            _this.sql.setSelectionRange(cursorPos, cursorPos);
            console.log("called back with: " + value);
        });
        return _this;
    }
    SqlPanel.prototype.setSql = function (sql) {
        this.sql.value = sql;
    };
    SqlPanel.prototype.setStatement = function (stmnt) {
        this.resultsDisplay.setStatement(stmnt);
    };
    SqlPanel.prototype.addResults = function (resp, filter) {
        if (resp === void 0) { resp = null; }
        this.resultsDisplay.addResults(resp, filter);
    };
    SqlPanel.processResults = function (metaData, objs) {
        var sql = objs.pop();
        console.log("executed: " + sql);
        var panel = ResultPanel.getInstance();
        var result = metaData.results[0];
        if (result.resultType == 'EXCEPTION') {
            panel.setStatement("Exception: " + result.toString);
            panel.addResults();
        }
        else {
            panel.setStatement("Statement: " + sql);
            panel.addResults(metaData);
        }
        panel.show();
        Menu.hideAllMenus();
    };
    SqlPanel.getInstance = function () {
        var headTxt = "SQL Panel " + Panel.nextPanelNumber();
        var x = new SqlPanel(PanelType[PanelType.Sql], headTxt);
        Panel.savePanel(x);
        return x;
    };
    return SqlPanel;
}(Panel));
SqlPanel.htmlStr = "\n        <div id=\"sqlPanel\">\n            <p class=\"blankLine\">&nbsp;</p>\n            <textarea id=\"statement\" placeholder=\"Type your SQL statement here ...\"></textarea>\n            <div id=\"sqlInput\">\n                <button id=\"exec\">execute</button>\n                <button id=\"clear\">clear</button>\n            </div>\n            <p class=\"blankLine\">&nbsp;</p>\n            <div id=\"sqlResults\"></div>\n        </div>\n    ";
SqlPanel.sqlTemplate = null;
var ResultPanel = (function (_super) {
    __extends(ResultPanel, _super);
    function ResultPanel(id, heading) {
        var _this = _super.call(this, PanelType.Result, id, heading) || this;
        _this.resultsDisplay = new ResultsDisplay(_this);
        return _this;
    }
    ResultPanel.prototype.setStatement = function (stmnt) {
        this.resultsDisplay.setStatement(stmnt);
    };
    ResultPanel.prototype.addResults = function (resp, filter) {
        if (resp === void 0) { resp = null; }
        this.resultsDisplay.addResults(resp, filter);
    };
    ResultPanel.getInstance = function () {
        var headTxt = "Result Panel " + Panel.nextPanelNumber();
        var x = new ResultPanel(PanelType[PanelType.Result], headTxt);
        Panel.savePanel(x);
        return x;
    };
    return ResultPanel;
}(Panel));
var ResultsDisplay = (function () {
    function ResultsDisplay(panel, cellTitle) {
        if (cellTitle === void 0) { cellTitle = null; }
        this.panel = panel;
        this.stmnt = document.createElement('p');
        this.stmnt.setAttribute('id', 'stmnt');
        this.panel.appendChild(this.stmnt);
        this.updateCnt = document.createElement('p');
        this.updateCnt.setAttribute('id', 'updateCnt');
        this.updateCnt.innerHTML = '';
        this.panel.appendChild(this.updateCnt);
        this.grid = document.createElement('div');
        this.grid.classList.add('scrollable');
        this.panel.appendChild(this.grid);
        this.cellTitle = cellTitle;
    }
    ResultsDisplay.prototype.setStatement = function (stmnt) {
        this.stmnt.innerHTML = stmnt;
    };
    ResultsDisplay.prototype.addValueCallback = function (valueCallback) {
        this.valueCallback = valueCallback;
    };
    ResultsDisplay.prototype.addResults = function (resp, filter) {
        var _this = this;
        if (resp === void 0) { resp = null; }
        if (filter === void 0) { filter = null; }
        this.updateCnt.innerHTML = '';
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        if (resp == null)
            return;
        var result = resp.results[0];
        if (result.updateCount != undefined) {
            this.updateCnt.innerHTML = 'Update Count: ' + String(result.updateCount);
            return;
        }
        var table = this.appendTable();
        var tr = this.appendRow(table);
        var td;
        this.appendHdrCell(tr, "Row");
        result.columnDefinitions.forEach(function (colDef, i, colDefs) {
            if (filter == null || (filter != null && filter.indexOf(i) >= 0))
                _this.appendHdrCell(tr, colDef.name);
        });
        var rowPanel;
        result.rows.forEach(function (row, index, rows) {
            tr = _this.createRowHtml(table, row, index, rowPanel, result);
            _this.appendCell(tr, String(index));
            row.values.forEach(function (value, i, values) {
                if (filter == null || (filter != null && filter.indexOf(i) >= 0)) {
                    td = _this.appendCell(tr);
                    _this.prepareDataCell(td, value);
                }
            });
        });
    };
    ResultsDisplay.prototype.createRowHtml = function (table, row, index, panel, result) {
        var _this = this;
        var tr = this.appendRow(table);
        tr.onclick = function (ev) {
            ev.stopPropagation();
            var head = _this.panel.getHeading() + ' Row ' + index;
            panel = RowPanel.getInstance();
            panel.addResults(result.columnDefinitions, row);
            panel.show();
            Menu.hideAllMenus();
        };
        return tr;
    };
    ResultsDisplay.prototype.prepareDataCell = function (td, value) {
        td.classList.add('nsItem');
        if (value.length > 32)
            td.innerHTML = value.substr(0, 32) + " ... ";
        else
            td.innerHTML = value;
    };
    ResultsDisplay.prototype.appendTable = function () {
        var _this = this;
        var table = document.createElement('table');
        this.grid.appendChild(table);
        if (this.valueCallback != null) {
            table.oncontextmenu = function (ev) {
                ev.preventDefault();
                ev.stopImmediatePropagation();
                var tdTgt = ev.target;
                _this.valueCallback(tdTgt.innerHTML);
            };
        }
        return table;
    };
    ResultsDisplay.prototype.appendRow = function (table) {
        var tr = document.createElement('tr');
        table.appendChild(tr);
        return tr;
    };
    ResultsDisplay.prototype.appendCell = function (tr, value) {
        if (value === void 0) { value = null; }
        var td;
        td = document.createElement('td');
        tr.appendChild(td);
        if (value != null)
            td.innerHTML = value;
        if (this.cellTitle != null)
            td.setAttribute('title', this.cellTitle);
        return td;
    };
    ResultsDisplay.prototype.appendHdrCell = function (tr, value) {
        if (value === void 0) { value = null; }
        var th;
        th = document.createElement('th');
        tr.appendChild(th);
        if (value != null)
            th.innerHTML = value;
        return th;
    };
    return ResultsDisplay;
}());
var Menu = (function () {
    function Menu() {
        var html = document.getElementById("bodyMenu");
        if (html == null) {
            html = Utils.makeDivFromString(Menu.bodyMenuStr);
            document.body.appendChild(html);
        }
        html = document.getElementById("panelListMenu");
        if (html == null) {
            html = Utils.makeDivFromString(Menu.panelListMenuStr);
            document.body.appendChild(html);
        }
        document.addEventListener("contextmenu", Menu.showContextMenu, false);
        document.addEventListener("click", Menu.hideAllMenus, false);
        var menuItems = document.querySelectorAll('#bodyMenu li');
        for (var i = 1; i < menuItems.length; i++)
            menuItems.item(i).addEventListener('click', Menu.itemClick, false);
    }
    Menu.showContextMenu = function (ev) {
        ev.preventDefault();
        var menu = document.getElementById('bodyMenu');
        var element = document.getElementById('menuCnnct');
        element.innerHTML = Panel.getPanel(PanelType[PanelType.Connect]).getHeading();
        Menu.showMenu(ev, 'bodyMenu');
    };
    Menu.showMenu = function (ev, menuName) {
        Menu.hideAllMenus();
        var menu = document.getElementById(menuName);
        menu.style.top = (ev.clientY - 15) + 'px';
        menu.style.left = ev.clientX + 'px';
        menu.style.display = 'block';
        menu.style.zIndex = Panel.newZindex();
    };
    Menu.hideAllMenus = function () {
        var menus = document.querySelectorAll('.menu');
        for (var i = 0; i < menus.length; i++)
            menus.item(i).style.display = 'none';
    };
    Menu.itemClick = function (ev) {
        ev.stopPropagation();
        Menu.hideAllMenus();
        var target = ev.target;
        switch (target.getAttribute("data-action")) {
            case PanelType[PanelType.Connect]:
                Panel.showPanel(PanelType[PanelType.Connect]);
                Menu.hideAllMenus();
                break;
            case PanelType[PanelType.Schema]:
                SchemaPanel.getInstance().show();
                Menu.hideAllMenus();
                break;
            case PanelType[PanelType.Sql]:
                SqlPanel.getInstance().show();
                Menu.hideAllMenus();
                break;
            case "sqls":
                Menu.showPanelListMenu(ev, PanelType.Sql, "SQL Panels");
                break;
            case "rows":
                Menu.showPanelListMenu(ev, PanelType.Row, "Row Panels");
                break;
            case "results":
                Menu.showPanelListMenu(ev, PanelType.Result, "Result Panels");
                break;
            default:
                var liAttrs = ev.target.attributes;
                var dataAction = liAttrs.getNamedItem('data-action');
                Panel.showPanel(dataAction.value);
        }
    };
    Menu.showPanelListMenu = function (ev, panelType, menuHeading) {
        var ul = document.getElementById('panelListMenu');
        while (ul.firstChild)
            ul.removeChild(ul.firstChild);
        var li = document.createElement('li');
        li.classList.add('menuHeading');
        li.innerHTML = menuHeading;
        ul.appendChild(li);
        var panels = Panel.getPanels();
        panels.forEach(function (panel, index, array) {
            if (panel.getType() === panelType) {
                li = document.createElement('li');
                li.innerHTML = panel.getHeading();
                var dataAction = document.createAttribute('data-action');
                dataAction.value = panel.getId();
                li.attributes.setNamedItem(dataAction);
                li.addEventListener('click', Menu.itemClick, false);
                ul.appendChild(li);
            }
        });
        if (ul.childElementCount > 1)
            Menu.showMenu(ev, 'panelListMenu');
    };
    return Menu;
}());
Menu.bodyMenuStr = "\n        <div>\n            <ul id=\"bodyMenu\" class=\"menu\">\n                <li class=\"menuHeading\">Context Menu</li>\n                <li id=\"menuCnnct\" data-action=\"Connect\">Connection Panel</li>\n                <li data-action=\"Schema\">Schema Panel</li>\n                <li data-action=\"Sql\">New SQL Panel</li>\n                <li data-action=\"sqls\">SQL Panels ...</li>\n                <li data-action=\"rows\">Row Panels ...</li>\n                <li data-action=\"results\">Result Panels ...</li>\n            </ul>\n        </div>\n    ";
Menu.panelListMenuStr = "\n        <div>\n            <ul id=\"panelListMenu\" class=\"menu\">\n                <li class=\"menuHeading\"></li>\n                <li data-action=\"metaPanel1\">Meta Panel 1</li>\n            </ul>\n        </div>\n    ";
var RowPanel = (function (_super) {
    __extends(RowPanel, _super);
    function RowPanel(id, heading) {
        var _this = _super.call(this, PanelType.Row, id, heading) || this;
        _this.grid = document.createElement('div');
        _super.prototype.appendChild.call(_this, _this.grid);
        _this.grid.classList.add('scrollable');
        return _this;
    }
    RowPanel.prototype.addResults = function (columnDefinitions, row) {
        while (this.grid.firstChild)
            this.grid.removeChild(this.grid.firstChild);
        var table = document.createElement('table');
        var tr = document.createElement('tr');
        var th = document.createElement('th');
        var td;
        this.grid.appendChild(table);
        table.appendChild(tr);
        tr.appendChild(th);
        th.innerText = 'Column';
        th = document.createElement('th');
        tr.appendChild(th);
        th.innerText = 'Value';
        row.values.forEach(function (value, i, values) {
            tr = document.createElement('tr');
            table.appendChild(tr);
            td = document.createElement('td');
            tr.appendChild(td);
            td.innerText = columnDefinitions[i].name;
            td = document.createElement('td');
            tr.appendChild(td);
            td.innerText = value;
        });
    };
    RowPanel.getInstance = function () {
        var headTxt = "Row Panel " + Panel.nextPanelNumber();
        var x = new RowPanel(PanelType[PanelType.Row], headTxt);
        Panel.savePanel(x);
        return x;
    };
    return RowPanel;
}(Panel));
