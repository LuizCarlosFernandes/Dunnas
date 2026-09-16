<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-bloco-detalhe">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="hero-card">
                <p class="eyebrow">Estrutura fisica</p>
                <h2>${bloco.identificacao}</h2>
                <div class="hero-metrics">
                    <span><strong>${bloco.quantidadeAndares}</strong> andares</span>
                    <span><strong>${bloco.apartamentosPorAndar}</strong> apartamentos por andar</span>
                </div>
            </section>

            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Geracao automatica</p>
                        <h2>Unidades do bloco</h2>
                    </div>
                    <a href="${ctx}/admin/blocos" class="btn btn-secondary">Voltar</a>
                </div>

                <c:choose>
                    <c:when test="${empty unidades}">
                        <div class="empty-state">
                            <h3>Nenhuma unidade encontrada</h3>
                            <p>Verifique se o bloco foi gerado corretamente.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Identificacao</th>
                                    <th>Andar</th>
                                    <th>Moradores vinculados</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${unidades}" var="unidade">
                                    <tr>
                                        <td>${unidade.identificacao}</td>
                                        <td>${unidade.andar}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${empty unidade.moradores}">
                                                    <span class="status-pill neutral">Sem moradores</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="stack-list">
                                                        <c:forEach items="${unidade.moradores}" var="morador">
                                                            <div>
                                                                <strong>${morador.nome}</strong>
                                                                <span>${morador.email}</span>
                                                            </div>
                                                        </c:forEach>
                                                    </div>
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
                    <c:if test="${unidadesPage.hasPrevious}">
                        <a class="btn btn-secondary" href="${ctx}/admin/blocos/${bloco.id}?page=${unidadesPage.page - 1}&size=${unidadesPage.size}">Anterior</a>
                    </c:if>
                    <span>Pagina ${unidadesPage.page + 1} de ${unidadesPage.totalPages == 0 ? 1 : unidadesPage.totalPages}</span>
                    <c:if test="${unidadesPage.hasNext}">
                        <a class="btn btn-secondary" href="${ctx}/admin/blocos/${bloco.id}?page=${unidadesPage.page + 1}&size=${unidadesPage.size}">Proxima</a>
                    </c:if>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
