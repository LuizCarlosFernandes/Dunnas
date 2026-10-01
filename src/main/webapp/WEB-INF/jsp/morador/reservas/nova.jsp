<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="morador-nova-reserva">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content narrow-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <%-- Passo 1: escolher area e data e consultar a disponibilidade (GET, sem CSRF na URL) --%>
            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Passo 1</p>
                        <h2>Escolha a area e a data</h2>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty areasDisponiveis}">
                        <div class="empty-state">
                            <h3>Nenhuma area comum disponivel</h3>
                            <p>O administrador ainda nao cadastrou areas comuns ativas para reserva.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <form method="get" action="${ctx}/morador/reservas/nova" class="filter-grid">
                            <label class="field">
                                <span>Area comum</span>
                                <select name="areaComumId" required>
                                    <option value="">Selecione uma area</option>
                                    <c:forEach items="${areasDisponiveis}" var="area">
                                        <option value="${area.id}" ${areaComumIdSelecionada eq area.id ? 'selected' : ''}><c:out value="${area.nome}"/></option>
                                    </c:forEach>
                                </select>
                            </label>
                            <label class="field">
                                <span>Data</span>
                                <input type="date" name="data" value="${dataSelecionada}" data-min-today required>
                            </label>
                            <div class="button-row align-end">
                                <button type="submit" class="btn btn-secondary">Ver disponibilidade</button>
                            </div>
                        </form>
                    </c:otherwise>
                </c:choose>
            </section>

            <c:if test="${not empty areaComumIdSelecionada and not empty dataSelecionada}">
                <section class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Disponibilidade</p>
                            <h2>Horarios ja reservados neste dia</h2>
                        </div>
                    </div>
                    <c:choose>
                        <c:when test="${empty intervalosOcupados}">
                            <div class="empty-state compact">
                                <p>Nenhuma reserva aprovada para esta area nesta data. O dia inteiro esta livre.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="stack-list">
                                <c:forEach items="${intervalosOcupados}" var="ocupado">
                                    <div class="busy-slot">
                                        <strong>${ocupado.horaInicioFormatada} - ${ocupado.horaFimFormatada}</strong>
                                        <span class="status-pill success">Ocupado</span>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <p class="field-hint">Solicitacoes ainda pendentes nao bloqueiam o horario: a disponibilidade e confirmada quando o administrador aprovar.</p>
                </section>

                <%-- Passo 2: confirmar o horario (POST) --%>
                <section class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Passo 2</p>
                            <h2>Informe o horario</h2>
                        </div>
                    </div>
                    <form method="post" action="${ctx}/morador/reservas" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <input type="hidden" name="areaComumId" value="${areaComumIdSelecionada}">
                        <input type="hidden" name="data" value="${dataSelecionada}">
                        <div class="form-grid">
                            <label class="field">
                                <span>Horario de inicio</span>
                                <input type="time" name="horaInicio" required>
                            </label>
                            <label class="field">
                                <span>Horario de fim</span>
                                <input type="time" name="horaFim" required>
                            </label>
                        </div>
                        <div class="button-row">
                            <button type="submit" class="btn btn-primary">Solicitar reserva</button>
                            <a href="${ctx}/morador/reservas" class="btn btn-secondary">Cancelar</a>
                        </div>
                    </form>
                </section>
            </c:if>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
<script>
    (function () {
        var today = new Date();
        var iso = today.getFullYear() + "-" + String(today.getMonth() + 1).padStart(2, "0") + "-" + String(today.getDate()).padStart(2, "0");
        document.querySelectorAll("input[data-min-today]").forEach(function (input) {
            input.min = iso;
        });
    })();
</script>
</body>
</html>
