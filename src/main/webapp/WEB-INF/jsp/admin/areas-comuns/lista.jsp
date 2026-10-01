<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-areas-comuns">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="two-column-grid">
                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Cadastro</p>
                            <h2>Nova area comum</h2>
                        </div>
                    </div>
                    <form method="post" action="${ctx}/admin/areas-comuns" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <label class="field">
                            <span>Nome</span>
                            <input type="text" name="nome" placeholder="Salao de festas" maxlength="255" required>
                        </label>
                        <button type="submit" class="btn btn-primary">Cadastrar area</button>
                    </form>
                </article>

                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Lista</p>
                            <h2>Areas cadastradas</h2>
                        </div>
                        <div class="toolbar-inline">
                            <input type="search" class="table-search" placeholder="Filtrar localmente" data-filter-input data-filter-target="areas-table">
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${empty areas}">
                            <div class="empty-state">
                                <h3>Nenhuma area cadastrada</h3>
                                <p>Cadastre a primeira area comum para que os moradores possam solicitar reservas.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="data-table" data-filter-table="areas-table">
                                    <thead>
                                    <tr>
                                        <th>Nome</th>
                                        <th>Status</th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${areas}" var="area">
                                        <tr>
                                            <td><c:out value="${area.nome}"/></td>
                                            <td>
                                                <span class="status-pill ${area.ativa ? 'success' : 'neutral'}">${area.ativa ? 'Ativa' : 'Inativa'}</span>
                                            </td>
                                            <td class="cell-actions">
                                                <a href="${ctx}/admin/areas-comuns/${area.id}" class="btn btn-link">Gerenciar</a>
                                                <c:choose>
                                                    <c:when test="${area.ativa}">
                                                        <form method="post" action="${ctx}/admin/areas-comuns/${area.id}" data-confirm="Desativar esta area? Ela deixara de aceitar novas solicitacoes.">
                                                            <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                            <input type="hidden" name="_method" value="delete">
                                                            <button type="submit" class="btn btn-secondary">Desativar</button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form method="post" action="${ctx}/admin/areas-comuns/${area.id}/reativacao">
                                                            <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                            <input type="hidden" name="_method" value="patch">
                                                            <button type="submit" class="btn btn-secondary">Reativar</button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="pagination">
                        <c:if test="${areasPage.hasPrevious}">
                            <a class="btn btn-secondary" href="${ctx}/admin/areas-comuns?page=${areasPage.page - 1}&size=${areasPage.size}">Anterior</a>
                        </c:if>
                        <span>Pagina ${areasPage.page + 1} de ${areasPage.totalPages == 0 ? 1 : areasPage.totalPages}</span>
                        <c:if test="${areasPage.hasNext}">
                            <a class="btn btn-secondary" href="${ctx}/admin/areas-comuns?page=${areasPage.page + 1}&size=${areasPage.size}">Proxima</a>
                        </c:if>
                    </div>
                </article>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
