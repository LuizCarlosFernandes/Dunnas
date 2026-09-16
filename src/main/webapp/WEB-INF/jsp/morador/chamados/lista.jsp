<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="morador-chamados">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Historico</p>
                        <h2>Meus chamados</h2>
                    </div>
                    <a href="${ctx}/morador/chamados/novo" class="btn btn-primary">Abrir chamado</a>
                </div>

                <form method="get" action="${ctx}/morador/chamados" class="filter-grid">
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
                        <span>Unidade</span>
                        <select name="unidadeId">
                            <option value="">Todas</option>
                            <c:forEach items="${unidadesDisponiveis}" var="unidade">
                                <option value="${unidade.id}" ${filtroUnidadeId eq unidade.id ? 'selected' : ''}>${unidade.identificacao}</option>
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
                        <span>Data de abertura</span>
                        <input type="date" name="dataAbertura" value="${filtroDataAbertura}">
                    </label>
                    <div class="button-row align-end">
                        <button type="submit" class="btn btn-primary">Filtrar</button>
                        <a href="${ctx}/morador/chamados" class="btn btn-secondary">Limpar</a>
                    </div>
                </form>

                <c:choose>
                    <c:when test="${empty chamados}">
                        <div class="empty-state">
                            <h3>Nenhum chamado registrado</h3>
                            <p>Use a abertura de chamado para registrar a primeira ocorrencia.</p>
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
                                <c:forEach items="${chamados}" var="chamado">
                                    <tr>
                                        <td>${chamado.unidadeIdentificacao}</td>
                                        <td>${chamado.tipoChamadoTitulo}</td>
                                        <td><span class="status-pill">${chamado.statusNome}</span></td>
                                        <td>${chamado.dataAberturaFormatada}</td>
                                        <td class="cell-actions">
                                            <a href="${ctx}/morador/chamados/${chamado.id}" class="btn btn-link">Acompanhar</a>
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
                        <a class="btn btn-secondary" href="${ctx}/morador/chamados?page=${chamadosPage.page - 1}&size=${chamadosPage.size}&statusId=${filtroStatusId}&unidadeId=${filtroUnidadeId}&tipoChamadoId=${filtroTipoChamadoId}&dataAbertura=${filtroDataAbertura}">Anterior</a>
                    </c:if>
                    <span>Pagina ${chamadosPage.page + 1} de ${chamadosPage.totalPages == 0 ? 1 : chamadosPage.totalPages}</span>
                    <c:if test="${chamadosPage.hasNext}">
                        <a class="btn btn-secondary" href="${ctx}/morador/chamados?page=${chamadosPage.page + 1}&size=${chamadosPage.size}&statusId=${filtroStatusId}&unidadeId=${filtroUnidadeId}&tipoChamadoId=${filtroTipoChamadoId}&dataAbertura=${filtroDataAbertura}">Proxima</a>
                    </c:if>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
