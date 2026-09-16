<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="colaborador-dashboard">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="stats-grid">
                <article class="stat-card">
                    <span>Chamados atrasados</span>
                    <strong>${totalChamadosAtrasados}</strong>
                </article>
                <article class="stat-card stat-card-wide">
                    <span>Chamados em atendimento</span>
                    <strong>${totalChamadosAbertos}</strong>
                    <a href="${ctx}/colaborador/chamados" class="btn btn-primary">Abrir fila</a>
                </article>
            </section>

            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Fila imediata</p>
                        <h2>Chamados recentes</h2>
                    </div>
                </div>

                <c:choose>
                    <c:when test="${empty chamados}">
                        <div class="empty-state">
                            <h3>Nenhum chamado disponivel no seu escopo</h3>
                            <p>Quando surgirem novos atendimentos eles aparecerao aqui.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Unidade</th>
                                    <th>Tipo</th>
                                    <th>Status</th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${chamados}" var="chamado">
                                    <tr>
                                        <td>${chamado.unidadeIdentificacao}</td>
                                        <td>${chamado.tipoChamadoTitulo}</td>
                                        <td><span class="status-pill">${chamado.statusNome}</span></td>
                                        <td class="cell-actions">
                                            <a href="${ctx}/colaborador/chamados/${chamado.id}" class="btn btn-link">Atender</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
