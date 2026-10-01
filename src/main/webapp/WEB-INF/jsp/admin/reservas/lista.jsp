<%@ include file="/WEB-INF/jsp/fragments/taglibs.jspf" %>
<!DOCTYPE html>
<html lang="pt-BR">
<%@ include file="/WEB-INF/jsp/fragments/head.jspf" %>
<body data-page="admin-reservas">
<div class="app-shell">
    <%@ include file="/WEB-INF/jsp/fragments/sidebar.jspf" %>
    <div class="app-main">
        <%@ include file="/WEB-INF/jsp/fragments/topbar.jspf" %>
        <main class="page-content">
            <%@ include file="/WEB-INF/jsp/fragments/alerts.jspf" %>

            <section class="card">
                <div class="section-header">
                    <div>
                        <p class="eyebrow">Areas comuns</p>
                        <h2>Reservas dos moradores</h2>
                    </div>
                    <a href="${ctx}/admin/areas-comuns" class="btn btn-secondary">Gerenciar areas</a>
                </div>

                <form method="get" action="${ctx}/admin/reservas" class="filter-grid">
                    <label class="field">
                        <span>Area comum</span>
                        <select name="areaComumId">
                            <option value="">Todas</option>
                            <c:forEach items="${areasFiltro}" var="area">
                                <option value="${area.id}" ${filtroAreaComumId eq area.id ? 'selected' : ''}><c:out value="${area.nome}"/></option>
                            </c:forEach>
                        </select>
                    </label>
                    <label class="field">
                        <span>Data</span>
                        <input type="date" name="data" value="${filtroData}">
                    </label>
                    <div class="button-row align-end">
                        <button type="submit" class="btn btn-primary">Filtrar</button>
                        <a href="${ctx}/admin/reservas" class="btn btn-secondary">Limpar</a>
                    </div>
                </form>

                <c:choose>
                    <c:when test="${empty reservas}">
                        <div class="empty-state">
                            <h3>Nenhuma reserva encontrada</h3>
                            <p>Quando os moradores solicitarem reservas, elas aparecerao aqui para aprovacao.</p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="table-wrap">
                            <table class="data-table">
                                <thead>
                                <tr>
                                    <th>Morador</th>
                                    <th>Area</th>
                                    <th>Data</th>
                                    <th>Horario</th>
                                    <th>Status</th>
                                    <th>Acoes</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${reservas}" var="reserva">
                                    <tr>
                                        <td><c:out value="${reserva.moradorNome}"/></td>
                                        <td><c:out value="${reserva.area.nome}"/></td>
                                        <td>${reserva.dataFormatada}</td>
                                        <td>${reserva.horaInicioFormatada} - ${reserva.horaFimFormatada}</td>
                                        <td>
                                            <span class="status-pill ${reserva.statusCodigo eq 'APROVADA' ? 'success' : (reserva.statusCodigo eq 'SOLICITADA' ? 'warning' : (reserva.statusCodigo eq 'NEGADA' ? 'danger' : 'neutral'))}">${reserva.statusLabel}</span>
                                            <c:if test="${reserva.statusCodigo eq 'NEGADA'}">
                                                <br><small class="field-hint">Motivo: <c:out value="${reserva.motivoNegacao}"/></small>
                                            </c:if>
                                        </td>
                                        <td class="cell-actions">
                                            <c:if test="${reserva.statusCodigo eq 'SOLICITADA'}">
                                                <form method="post" action="${ctx}/admin/reservas/${reserva.id}/aprovacao">
                                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                    <input type="hidden" name="_method" value="patch">
                                                    <button type="submit" class="btn btn-primary">Aprovar</button>
                                                </form>
                                                <details class="inline-details">
                                                    <summary class="btn btn-secondary">Negar</summary>
                                                    <form method="post" action="${ctx}/admin/reservas/${reserva.id}/negacao">
                                                        <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                        <input type="hidden" name="_method" value="patch">
                                                        <input type="text" name="motivo" placeholder="Motivo da negacao" maxlength="255" required>
                                                        <button type="submit" class="btn btn-danger">Confirmar negacao</button>
                                                    </form>
                                                </details>
                                            </c:if>
                                            <c:if test="${reserva.cancelavel}">
                                                <form method="post" action="${ctx}/admin/reservas/${reserva.id}/cancelamento" data-confirm="Deseja cancelar esta reserva?">
                                                    <%@ include file="/WEB-INF/jsp/fragments/csrf.jspf" %>
                                                    <input type="hidden" name="_method" value="patch">
                                                    <button type="submit" class="btn btn-secondary">Cancelar</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="pagination">
                    <c:if test="${reservasPage.hasPrevious}">
                        <a class="btn btn-secondary" href="${ctx}/admin/reservas?page=${reservasPage.page - 1}&size=${reservasPage.size}&areaComumId=${filtroAreaComumId}&data=${filtroData}">Anterior</a>
                    </c:if>
                    <span>Pagina ${reservasPage.page + 1} de ${reservasPage.totalPages == 0 ? 1 : reservasPage.totalPages}</span>
                    <c:if test="${reservasPage.hasNext}">
                        <a class="btn btn-secondary" href="${ctx}/admin/reservas?page=${reservasPage.page + 1}&size=${reservasPage.size}&areaComumId=${filtroAreaComumId}&data=${filtroData}">Proxima</a>
                    </c:if>
                </div>
            </section>
        </main>
    </div>
</div>
<%@ include file="/WEB-INF/jsp/fragments/scripts.jspf" %>
</body>
</html>
