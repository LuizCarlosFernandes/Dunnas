<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-usuarios">
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
                            <p class="eyebrow">Gestao de acesso</p>
                            <h2>Novo usuario</h2>
                        </div>
                    </div>
                    <form method="post" action="${ctx}/admin/usuarios" class="stack-form">
                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                        <label class="field">
                            <span>Nome</span>
                            <input type="text" name="nome" value="${usuarioForm.nome}" maxlength="255" required>
                        </label>
                        <label class="field">
                            <span>Email</span>
                            <input type="email" name="email" value="${usuarioForm.email}" maxlength="255" required>
                        </label>
                        <label class="field">
                            <span>Tipo</span>
                            <select name="tipo" required>
                                <option value="">Selecione</option>
                                <c:forEach items="${tiposUsuario}" var="tipo">
                                    <option value="${tipo.key}" ${usuarioForm.tipo eq tipo.key ? 'selected' : ''}>${tipo.value}</option>
                                </c:forEach>
                            </select>
                        </label>
                        <label class="field">
                            <span>Senha inicial</span>
                            <div class="password-field">
                                <input type="password" name="senha" maxlength="255" required data-password-input>
                                <button type="button" class="ghost-button" data-password-toggle>Mostrar</button>
                            </div>
                        </label>
                        <button type="submit" class="btn btn-primary">Cadastrar usuario</button>
                    </form>
                </article>

                <article class="card">
                    <div class="section-header">
                        <div>
                            <p class="eyebrow">Base cadastrada</p>
                            <h2>Usuarios</h2>
                        </div>
                        <input type="search" class="table-search" placeholder="Filtrar localmente" data-filter-input data-filter-target="usuarios-table">
                    </div>

                    <c:choose>
                        <c:when test="${empty usuarios}">
                            <div class="empty-state">
                                <h3>Nenhum usuario encontrado</h3>
                                <p>Cadastre administradores, colaboradores e moradores para iniciar a operacao.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-wrap">
                                <table class="data-table" data-filter-table="usuarios-table">
                                    <thead>
                                    <tr>
                                        <th>Nome</th>
                                        <th>Email</th>
                                        <th>Perfil</th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <c:forEach items="${usuarios}" var="usuario">
                                        <tr>
                                            <td>${usuario.nome}</td>
                                            <td>${usuario.email}</td>
                                            <td><span class="status-pill neutral">${usuario.tipo}</span></td>
                                            <td class="cell-actions">
                                                <a href="${ctx}/admin/usuarios/${usuario.id}" class="btn btn-link">Gerenciar</a>
                                                <form method="post" action="${ctx}/admin/usuarios/${usuario.id}" onsubmit="return confirm('Deseja desativar este usuario?');">
                                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                    <input type="hidden" name="_method" value="delete">
                                                    <button type="submit" class="btn btn-secondary">Desativar</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <div class="pagination">
                        <c:if test="${usuariosPage.hasPrevious}">
                            <a class="btn btn-secondary" href="${ctx}/admin/usuarios?page=${usuariosPage.page - 1}&size=${usuariosPage.size}">Anterior</a>
                        </c:if>
                        <span>Pagina ${usuariosPage.page + 1} de ${usuariosPage.totalPages == 0 ? 1 : usuariosPage.totalPages}</span>
                        <c:if test="${usuariosPage.hasNext}">
                            <a class="btn btn-secondary" href="${ctx}/admin/usuarios?page=${usuariosPage.page + 1}&size=${usuariosPage.size}">Proxima</a>
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
