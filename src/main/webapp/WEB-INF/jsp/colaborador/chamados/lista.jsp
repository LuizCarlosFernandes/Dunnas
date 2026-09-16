<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="colaborador-chamados">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Atendimento</p>
                        <h2>Fila de chamados</h2>
                        <p class="section-subtitle">Os chamados mais antigos ficam no topo para priorizar a fila.</p>
                    </div>
                </div>

                <form method="get" action="${ctx}/colaborador/chamados" class="filter-grid">
                    <label class="field">
                        <span>Status</span>
                        <select name="statusId">
                            <option value="">Todos</option>
                            <c:forEach items="${statusDisponiveis}" var="status">
                                <option value="${status.id}" ${filtroStatusId eq status.id ? 'selected' : ''}>${status.nome}</option>
                            </c:forEach>
                        </select>
                    </label>
                    <label class="field">
                        <span>Tipo</span>
                        <select name="tipoChamadoId">
                            <option value="">Todos</option>
                            <c:forEach items="${tiposChamadoDisponiveis}" var="tipo">
                                <option value="${tipo.id}" ${filtroTipoChamadoId eq tipo.id ? 'selected' : ''}>${tipo.titulo}</option>
                            </c:forEach>
                        </select>
                    </label>
                    <label class="field">
                        <span>Pesquisar unidade</span>
                        <input type="text" name="unidade" value="${filtroUnidade}" placeholder="Ex.: 101">
                    </label>
                    <label class="field">
                        <span>Data de abertura</span>
                        <input type="date" name="dataAbertura" value="${filtroDataAbertura}">
                    </label>
                    <div class="button-row align-end">
                        <button type="submit" class="btn btn-primary">Filtrar</button>
                        <a href="${ctx}/colaborador/chamados" class="btn btn-secondary">Limpar</a>
                    </div>
                </form>
            </section>

            <section class="card">
                <c:choose>
                    <c:when test="${empty chamados}">
                        <div class="empty-state">
                            <h3>Nenhum chamado encontrado</h3>
                            <p>Revise os filtros ou aguarde novas ocorrencias no seu escopo.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Unidade</th>
                                    <th>Morador</th>
                                    <th>Tipo</th>
                                    <th>Status</th>
                                    <th>Abertura</th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${chamados}" var="chamado">
                                    <tr>
                                        <td>${chamado.unidadeIdentificacao}</td>
                                        <td>${chamado.moradorNome}</td>
                                        <td>${chamado.tipoChamadoTitulo}</td>
                                        <td><span class="status-pill">${chamado.statusNome}</span></td>
                                        <td>${chamado.dataAberturaFormatada}</td>
                                        <td class="cell-actions">
                                            <a href="${ctx}/colaborador/chamados/${chamado.id}" class="btn btn-link">Detalhar</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="pagination">
                    <c:if test="${chamadosPage.hasPrevious}">
                        <a class="btn btn-secondary" href="${ctx}/colaborador/chamados?page=${chamadosPage.page - 1}&size=${chamadosPage.size}&statusId=${filtroStatusId}&tipoChamadoId=${filtroTipoChamadoId}&unidade=${filtroUnidade}&dataAbertura=${filtroDataAbertura}">Anterior</a>
                    </c:if>
                    <span>Pagina ${chamadosPage.page + 1} de ${chamadosPage.totalPages == 0 ? 1 : chamadosPage.totalPages}</span>
                    <c:if test="${chamadosPage.hasNext}">
                        <a class="btn btn-secondary" href="${ctx}/colaborador/chamados?page=${chamadosPage.page + 1}&size=${chamadosPage.size}&statusId=${filtroStatusId}&tipoChamadoId=${filtroTipoChamadoId}&unidade=${filtroUnidade}&dataAbertura=${filtroDataAbertura}">Proxima</a>
                    </c:if>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
