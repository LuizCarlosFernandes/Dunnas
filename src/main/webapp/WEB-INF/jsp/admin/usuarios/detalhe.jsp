<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-usuario-detalhe">
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
                            <h2>${usuario.nome}</h2>
                        </div>
                        <span class="status-pill neutral">${usuario.tipo}</span>
                    </div>

                    <form method="post" action="${ctx}/admin/usuarios/${usuario.id}" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <input type="hidden" name="_method" value="patch">
                        <label class="field">
                            <span>Nome</span>
                            <input type="text" name="nome" value="${usuarioForm.nome}" maxlength="255" required>
                        </label>
                        <label class="field">
                            <span>Email</span>
                            <input type="email" name="email" value="${usuarioForm.email}" maxlength="255" required>
                        </label>
                        <label class="field">
                            <span>Perfil</span>
                            <input type="text" value="${usuario.tipo}" disabled>
                            <input type="hidden" name="tipo" value="${usuarioForm.tipo}">
                        </label>
                        <label class="field">
                            <span>Nova senha</span>
                            <div class="password-field">
                                <input type="password" name="senha" placeholder="Obrigatorio para salvar" maxlength="255" required data-password-input>
                                <button type="button" class="ghost-button" data-password-toggle>Mostrar</button>
                            </div>
                        </label>
                        <div class="button-row">
                            <button type="submit" class="btn btn-primary">Salvar alteracoes</button>
                            <a href="${ctx}/admin/usuarios" class="btn btn-secondary">Voltar</a>
                        </div>
                    </form>

                    <form method="post" action="${ctx}/admin/usuarios/${usuario.id}" data-confirm="Remover este usuario? A acao nao pode ser desfeita." class="inline-form danger-zone">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <input type="hidden" name="_method" value="delete">
                        <button type="submit" class="btn btn-danger">Remover usuario</button>
                    </form>
                </article>

                <c:if test="${usuario.role eq 'ROLE_MORADOR'}">
                    <article class="card">
                        <div class="section-header">
                            <div>
                                <p class="eyebrow">Vinculos</p>
                                <h2>Unidades do morador</h2>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${empty unidadesMorador}">
                                <div class="empty-state compact">
                                    <p>Nenhuma unidade vinculada.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stack-list">
                                    <c:forEach items="${unidadesMorador}" var="unidade">
                                        <div class="list-row">
                                            <div>
                                                <strong>${unidade.identificacao}</strong>
                                                <span>${unidade.blocoIdentificacao} - Andar ${unidade.andar}</span>
                                            </div>
                                            <form method="post" action="${ctx}/admin/moradores/${usuario.id}/unidades/${unidade.id}" data-confirm="Desvincular esta unidade do morador?" class="inline-form">
                                                <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                <input type="hidden" name="_method" value="delete">
                                                <c:if test="${not empty blocoSelecionadoId}">
                                                    <input type="hidden" name="blocoId" value="${blocoSelecionadoId}">
                                                </c:if>
                                                <button type="submit" class="btn btn-danger">Desvincular</button>
                                            </form>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="divider"></div>

                        <form method="get" action="${ctx}/admin/usuarios/${usuario.id}" class="stack-form compact-form">
                            <label class="field">
                                <span>Selecionar bloco para vincular</span>
                                <select name="blocoId" data-auto-submit>
                                    <option value="">Escolha um bloco</option>
                                    <c:forEach items="${blocosDisponiveis}" var="bloco">
                                        <option value="${bloco.id}" ${blocoSelecionadoId eq bloco.id ? 'selected' : ''}>
                                            ${bloco.identificacao}
                                        </option>
                                    </c:forEach>
                                </select>
                            </label>
                        </form>

                        <c:if test="${not empty unidadesBloco}">
                            <form method="post" action="${ctx}/admin/moradores/${usuario.id}/unidades" class="stack-form compact-form">
                                <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                <input type="hidden" name="_method" value="put">
                                <input type="hidden" name="blocoId" value="${blocoSelecionadoId}">
                                <label class="field">
                                    <span>Selecionar unidade</span>
                                    <select name="unidadeId" required>
                                        <option value="">Escolha uma unidade</option>
                                        <c:forEach items="${unidadesBloco}" var="unidade">
                                            <option value="${unidade.id}" ${unidade.vinculadaAoMorador ? 'disabled' : ''}>
                                                ${unidade.identificacao} - Andar ${unidade.andar}
                                                ${unidade.vinculadaAoMorador ? ' (ja vinculada)' : ''}
                                            </option>
                                        </c:forEach>
                                    </select>
                                </label>
                                <button type="submit" class="btn btn-primary">Vincular unidade</button>
                            </form>

                            <div class="table-wrap">
                                <table class="data-table compact-table">
                                    <thead>
                                    <tr>
                                        <th>Unidade</th>
                                        <th>Andar</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${unidadesBloco}" var="unidade">
                                        <tr>
                                            <td>${unidade.identificacao}</td>
                                            <td>${unidade.andar}</td>
                                            <td>
                                                <span class="status-pill ${unidade.vinculadaAoMorador ? '' : 'neutral'}">
                                                    ${unidade.vinculadaAoMorador ? 'Vinculada' : 'Disponivel'}
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:if>
                    </article>
                </c:if>

                <c:if test="${usuario.role eq 'ROLE_COLABORADOR'}">
                    <article class="card">
                        <div class="section-header">
                            <div>
                                <p class="eyebrow">Escopo</p>
                                <h2>Tipos de chamado do colaborador</h2>
                            </div>
                        </div>

                        <c:choose>
                            <c:when test="${empty tiposChamadoColaborador}">
                                <div class="empty-state compact">
                                    <p>Nenhum tipo de chamado vinculado.</p>
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
                                            <form method="post" action="${ctx}/admin/colaboradores/${usuario.id}/tipos-chamado/${tipoChamado.id}" data-confirm="Desvincular este tipo de chamado do colaborador?" class="inline-form">
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

                        <form method="post" action="${ctx}/admin/colaboradores/${usuario.id}/tipos-chamado" class="stack-form compact-form">
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
                    </article>
                </c:if>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
