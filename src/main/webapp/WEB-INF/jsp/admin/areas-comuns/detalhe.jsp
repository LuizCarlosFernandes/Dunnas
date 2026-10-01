<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-area-comum-detalhe">
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
                            <p class="eyebrow">Edicao</p>
                            <h2><c:out value="${area.nome}"/></h2>
                        </div>
                        <span class="status-pill ${area.ativa ? 'success' : 'neutral'}">${area.ativa ? 'Ativa' : 'Inativa'}</span>
                    </div>
                    <form method="post" action="${ctx}/admin/areas-comuns/${area.id}" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <input type="hidden" name="_method" value="patch">
                        <label class="field">
                            <span>Nome</span>
                            <input type="text" name="nome" value="<c:out value='${area.nome}'/>" maxlength="255" required>
                        </label>
                        <div class="button-row">
                            <button type="submit" class="btn btn-primary">Salvar alteracoes</button>
                            <a href="${ctx}/admin/areas-comuns" class="btn btn-secondary">Voltar</a>
                        </div>
                    </form>
                </article>

                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Disponibilidade</p>
                            <h2>Situacao da area</h2>
                        </div>
                    </div>
                    <div class="stack-list">
                        <c:choose>
                            <c:when test="${area.ativa}">
                                <p>Esta area aceita novas solicitacoes de reserva. Ao desativar, as reservas ja existentes sao mantidas, mas novas solicitacoes deixam de ser aceitas.</p>
                                <form method="post" action="${ctx}/admin/areas-comuns/${area.id}" data-confirm="Desativar esta area? Ela deixara de aceitar novas solicitacoes.">
                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                    <input type="hidden" name="_method" value="delete">
                                    <button type="submit" class="btn btn-danger">Desativar area</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <p>Esta area esta desativada e nao aceita novas solicitacoes de reserva.</p>
                                <form method="post" action="${ctx}/admin/areas-comuns/${area.id}/reativacao">
                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                    <input type="hidden" name="_method" value="patch">
                                    <button type="submit" class="btn btn-primary">Reativar area</button>
                                </form>
                            </c:otherwise>
                        </c:choose>
                        <a href="${ctx}/admin/reservas?areaComumId=${area.id}" class="btn btn-secondary">Ver reservas desta area</a>
                    </div>
                </article>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
