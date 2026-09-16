<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-status">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>
            <c:set var="statusChamadoAction" value="${ctx}/admin/status-chamado" />
            <c:if test="${not empty statusEdicao}">
                <c:set var="statusChamadoAction" value="${ctx}/admin/status-chamado/${statusEdicao.id}" />
            </c:if>
            <c:set var="statusEdicaoBloqueada" value="${statusEdicaoBloqueada eq true}" />

            <section class="two-column-grid">
                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Fluxo</p>
                            <h2><c:choose><c:when test="${not empty statusEdicao}">Editar status</c:when><c:otherwise>Novo status</c:otherwise></c:choose></h2>
                        </div>
                    </div>

                    <form method="post" action="${statusChamadoAction}" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <c:if test="${not empty statusEdicao}">
                            <input type="hidden" name="_method" value="patch">
                        </c:if>
                        <label class="field">
                            <span>Nome do status</span>
                            <input type="text" name="nome" value="${statusChamadoForm.nome}" placeholder="Em atendimento" maxlength="255" required ${statusEdicaoBloqueada ? 'disabled' : ''}>
                        </label>
                        <c:if test="${statusEdicaoBloqueada}">
                            <p class="helper-text">Os status Finalizado, Atrasado e Solicitado sao reservados e nao podem ser editados.</p>
                        </c:if>
                        <div class="button-row">
                            <button type="submit" class="btn btn-primary" ${statusEdicaoBloqueada ? 'disabled' : ''}>
                                <c:choose><c:when test="${not empty statusEdicao}">Salvar status</c:when><c:otherwise>Cadastrar status</c:otherwise></c:choose>
                            </button>
                            <c:if test="${not empty statusEdicao}">
                                <a href="${ctx}/admin/status-chamado" class="btn btn-secondary">Cancelar</a>
                            </c:if>
                        </div>
                    </form>
                </article>

                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Estados do chamado</p>
                            <h2>Status configurados</h2>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${empty statusChamado}">
                            <div class="empty-state">
                                <h3>Nenhum status cadastrado</h3>
                                <p>Cadastre ao menos um status e marque o inicial padrao.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="stack-list">
                                <c:forEach items="${statusChamado}" var="status">
                                    <div class="list-row status-row">
                                        <div>
                                            <strong>${status.nome}</strong>
                                            <span><c:if test="${status.inicialPadrao}">Status inicial padrao</c:if><c:if test="${not status.inicialPadrao}">Disponivel para fluxo operacional</c:if></span>
                                        </div>
                                        <div class="button-row">
                                            <c:if test="${status.editavel}">
                                                <a href="${ctx}/admin/status-chamado?statusId=${status.id}" class="btn btn-secondary">Editar</a>
                                            </c:if>
                                            <c:if test="${not status.editavel}">
                                                <span class="btn btn-secondary disabled" aria-disabled="true">Reservado</span>
                                            </c:if>
                                            <form method="post" action="${ctx}/admin/status-chamado/${status.id}/inicial-padrao" class="inline-form" data-confirm="Definir este status como inicial padrao?">
                                                <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                <input type="hidden" name="_method" value="patch">
                                                <button type="submit" class="btn btn-primary" ${status.inicialPadrao ? 'disabled' : ''}>
                                                    Tornar padrao
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="pagination">
                        <c:if test="${statusChamadoPage.hasPrevious}">
                            <a class="btn btn-secondary" href="${ctx}/admin/status-chamado?page=${statusChamadoPage.page - 1}&size=${statusChamadoPage.size}">Anterior</a>
                        </c:if>
                        <span>Pagina ${statusChamadoPage.page + 1} de ${statusChamadoPage.totalPages == 0 ? 1 : statusChamadoPage.totalPages}</span>
                        <c:if test="${statusChamadoPage.hasNext}">
                            <a class="btn btn-secondary" href="${ctx}/admin/status-chamado?page=${statusChamadoPage.page + 1}&size=${statusChamadoPage.size}">Proxima</a>
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
