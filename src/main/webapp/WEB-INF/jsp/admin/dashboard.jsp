<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-dashboard">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="stats-grid">
                <article class="stat-card">
                    <span>Blocos</span>
                    <strong>${totalBlocos}</strong>
                </article>
                <article class="stat-card">
                    <span>Usuarios</span>
                    <strong>${totalUsuarios}</strong>
                </article>
                <article class="stat-card">
                    <span>Tipos de Chamado</span>
                    <strong>${totalTiposChamado}</strong>
                </article>
                <article class="stat-card">
                    <span>Status</span>
                    <strong>${totalStatus}</strong>
                </article>
                <article class="stat-card">
                    <span>Chamados atrasados</span>
                    <strong>${totalChamadosAtrasados}</strong>
                </article>
                <article class="stat-card stat-card-wide">
                    <span>Chamados monitorados</span>
                    <strong>${totalChamados}</strong>
                    <a href="${ctx}/admin/chamados" class="btn btn-secondary">Abrir fila completa</a>
                </article>
            </section>

            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Visao operacional</p>
                        <h2>Chamados recentes</h2>
                    </div>
                    <a href="${ctx}/admin/chamados" class="btn btn-primary">Ver todos</a>
                </div>

                <c:choose>
                    <c:when test="${empty chamadosRecentes}">
                        <div class="empty-state">
                            <h3>Nenhum chamado registrado</h3>
                            <p>Assim que moradores abrirem chamados eles aparecerao aqui.</p>
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
                                    <th>Abertura</th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${chamadosRecentes}" var="chamado">
                                    <tr>
                                        <td>${chamado.unidadeIdentificacao}</td>
                                        <td>${chamado.tipoChamadoTitulo}</td>
                                        <td><span class="status-pill">${chamado.statusNome}</span></td>
                                        <td>${chamado.dataAberturaFormatada}</td>
                                        <td class="cell-actions">
                                            <a href="${ctx}/admin/chamados/${chamado.id}" class="btn btn-link">Detalhar</a>
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
