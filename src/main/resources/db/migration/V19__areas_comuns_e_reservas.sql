create table areas_comuns(
    id uuid not null primary key,
    nome varchar(255) not null,
    ativa boolean not null default true
)

create table reservas(
    id uuid not null primary key,
    area_comum_id uuid not null references areas_comuns(id),
    morador_id uuid not null references moradores(id),
    data date not null,
    hora_inicio time not null,
    hora_fim time not null,
    status varchar(20) not null,
    motivo_negacao varchar(500),
    data_criacao timestamp(6),
    data_decisao timestamp(6)
)

create index idx_reservas_area_data on reservas (area_comum_id, data);