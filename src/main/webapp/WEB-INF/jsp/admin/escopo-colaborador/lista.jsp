<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-escopo-colaborador">
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
                            <p class="eyebrow">Operacao</p>
                            <h2>Designar colaborador por tipo</h2>
                        </div>
                    </div>

                    <form method="get" action="${ctx}/admin/escopo-colaborador" class="stack-form compact-form">
                        <label class="field">
                            <span>Buscar por e-mail</span>
                            <input type="text" name="colaboradorEmail" value="${filtroColaboradorEmail}" placeholder="Ex.: ana" />
                        </label>
                        <label class="field">
                            <span>Colaborador</span>
                            <select name="colaboradorId" data-auto-submit>
                                <option value="">Escolha um colaborador</option>
                                <c:forEach items="${colaboradoresDisponiveis}" var="colaborador">
                                    <option value="${colaborador.id}" ${colaboradorSelecionadoId eq colaborador.id ? 'selected' : ''}>
                                        ${colaborador.nome} - ${colaborador.email}
                                    </option>
                                </c:forEach>
                            </select>
                        </label>
                        <div class="button-row">
                            <button type="submit" class="btn btn-primary">Buscar colaborador</button>
                            <a href="${ctx}/admin/escopo-colaborador" class="btn btn-secondary">Limpar</a>
                        </div>
                    </form>

                    <c:if test="${not empty colaboradorSelecionado}">
                        <div class="divider"></div>
                        <div class="list-row">
                            <div>
                                <strong>${colaboradorSelecionado.nome}</strong>
                                <span>${colaboradorSelecionado.email}</span>
                            </div>
                            <a href="${ctx}/admin/usuarios/${colaboradorSelecionado.id}" class="btn btn-secondary">Abrir cadastro</a>
                        </div>

                        <c:choose>
                            <c:when test="${empty tiposChamadoColaborador}">
                                <div class="empty-state compact">
                                    <p>Este colaborador ainda nao possui tipos de chamado vinculados.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stack-list">
                                    <c:forEach items="${tiposChamadoColaborador}" var="tipoChamado">
                                        <div class="list-row">
                                            <div>
                                                <strong>${tipoChamado.titulo}</strong>
                                                <span>Prazo: ${tipoChamado.prazoHoras}h</span>
                                            </div>
                                            <form method="post" action="${ctx}/admin/colaboradores/${colaboradorSelecionado.id}/tipos-chamado/${tipoChamado.id}?dashboard=true" data-confirm="Desvincular este tipo de chamado do colaborador?" class="inline-form">
                                                <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                <input type="hidden" name="_method" value="delete">
                                                <button type="submit" class="btn btn-danger">Desvincular</button>
                                            </form>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="divider"></div>

                        <form method="post" action="${ctx}/admin/colaboradores/${colaboradorSelecionado.id}/tipos-chamado?dashboard=true" class="stack-form compact-form">
                            <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                            <input type="hidden" name="_method" value="put">
                            <label class="field">
                                <span>Selecionar tipo de chamado</span>
                                <select name="tipoChamadoId" required>
                                    <option value="">Escolha um tipo</option>
                                    <c:forEach items="${tiposChamadoDisponiveis}" var="tipoChamado">
                                        <c:set var="tipoChamadoJaVinculado" value="${tiposChamadoResponsaveisIds.contains(tipoChamado.id)}" />
                                        <option value="${tipoChamado.id}" ${tipoChamadoJaVinculado ? 'disabled' : ''}>
                                            ${tipoChamado.titulo} - ${tipoChamado.prazoHoras}h
                                            ${tipoChamadoJaVinculado ? ' (ja vinculado)' : ''}
                                        </option>
                                    </c:forEach>
                                </select>
                            </label>
                            <button type="submit" class="btn btn-primary">Vincular tipo de chamado</button>
                        </form>
                    </c:if>
                </article>

                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Consulta</p>
                            <h2>Colaboradores encontrados</h2>
                        </div>
                    </div>

                    <c:choose>
                        <c:when test="${empty colaboradoresDisponiveis}">
                            <div class="empty-state">
                                <h3>Nenhum colaborador encontrado</h3>
                                <p>Ajuste o prefixo do e-mail para localizar outro colaborador.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="data-table">
                                    <thead>
                                    <tr>
                                        <th>Nome</th>
                                        <th>Email</th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${colaboradoresDisponiveis}" var="colaborador">
                                        <c:url var="selecionarColaboradorUrl" value="/admin/escopo-colaborador">
                                            <c:param name="colaboradorId" value="${colaborador.id}" />
                                            <c:if test="${not empty filtroColaboradorEmail}">
                                                <c:param name="colaboradorEmail" value="${filtroColaboradorEmail}" />
                                            </c:if>
                                        </c:url>
                                        <tr>
                                            <td>${colaborador.nome}</td>
                                            <td>${colaborador.email}</td>
                                            <td class="cell-actions">
                                                <a href="${selecionarColaboradorUrl}" class="btn btn-link">Selecionar</a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </article>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
