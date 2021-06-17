

CREATE TABLE public.campaign (
    id character varying(50) NOT NULL,
    label character varying(255) NOT NULL
);


CREATE TABLE public.comment (
    id uuid NOT NULL,
    value jsonb,
    survey_unit_id character varying(255)
);
CREATE TABLE public.data (
    id uuid NOT NULL,
    value jsonb,
    survey_unit_id character varying(255)
);

CREATE TABLE public.metadata (
    id uuid NOT NULL,
    value jsonb,
    campaign_id character varying(50)
);

CREATE TABLE public.nomenclature (
    id character varying(50) NOT NULL,
    label character varying(255) NOT NULL,
    value jsonb
);


CREATE TABLE public.paradata_event (
    id uuid NOT NULL,
    value jsonb
);

CREATE TABLE public.personalization (
    id uuid NOT NULL,
    value jsonb,
    survey_unit_id character varying(255)
);

CREATE TABLE public.questionnaire_model (
    id character varying(50) NOT NULL,
    label character varying(255) NOT NULL,
    value jsonb,
    campaign_id character varying(50)
);

CREATE TABLE public.required_nomenclature (
    id_required_nomenclature character varying(50) NOT NULL,
    code character varying(50) NOT NULL
);

CREATE TABLE public.state_data (
    id uuid NOT NULL,
    current_page character varying(20),
    date bigint,
    state character varying(9),
    survey_unit_id character varying(255)
);

CREATE TABLE public.survey_unit (
    id character varying(255) NOT NULL,
    campaign_id character varying(50),
    questionnaire_model_id character varying(50)
);


ALTER TABLE ONLY public.campaign
    ADD CONSTRAINT "campaignPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT "commentPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.data
    ADD CONSTRAINT "dataPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.metadata
    ADD CONSTRAINT "metadataPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.nomenclature
    ADD CONSTRAINT "nomenclaturePK" PRIMARY KEY (id);

ALTER TABLE ONLY public.paradata_event
    ADD CONSTRAINT "paradata_eventPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.personalization
    ADD CONSTRAINT "personalizationPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.questionnaire_model
    ADD CONSTRAINT "questionnaire_modelPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.required_nomenclature
    ADD CONSTRAINT required_nomenclature_pkey PRIMARY KEY (id_required_nomenclature, code);

ALTER TABLE ONLY public.state_data
    ADD CONSTRAINT "state_dataPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.survey_unit
    ADD CONSTRAINT "survey_unitPK" PRIMARY KEY (id);

ALTER TABLE ONLY public.metadata
    ADD CONSTRAINT "FK13bif89tkws06lu6kdo3nye0t" FOREIGN KEY (campaign_id) REFERENCES public.campaign(id);

ALTER TABLE ONLY public.survey_unit
    ADD CONSTRAINT "FK5npgv34xrt4sot2mv05ij3tse" FOREIGN KEY (campaign_id) REFERENCES public.campaign(id);

ALTER TABLE ONLY public.data
    ADD CONSTRAINT "FK7ym9pbkxwahn9vpf2fgoaxxuq" FOREIGN KEY (survey_unit_id) REFERENCES public.survey_unit(id);

ALTER TABLE ONLY public.personalization
    ADD CONSTRAINT "FK9aonche3cbcolkeuacv4v6hk" FOREIGN KEY (survey_unit_id) REFERENCES public.survey_unit(id);

ALTER TABLE ONLY public.required_nomenclature
    ADD CONSTRAINT "FKcjxyxys9mk6ym2kmwignxw7kp" FOREIGN KEY (id_required_nomenclature) REFERENCES public.questionnaire_model(id);

ALTER TABLE ONLY public.state_data
    ADD CONSTRAINT "FKkjjh680qs400ap1dko1kmqh0s" FOREIGN KEY (survey_unit_id) REFERENCES public.survey_unit(id);

ALTER TABLE ONLY public.required_nomenclature
    ADD CONSTRAINT "FKlusjrt37f9351fracyajgflj2" FOREIGN KEY (code) REFERENCES public.nomenclature(id);

ALTER TABLE ONLY public.comment
    ADD CONSTRAINT "FKmp8mo44go4vhohovjaxxg8140" FOREIGN KEY (survey_unit_id) REFERENCES public.survey_unit(id);

ALTER TABLE ONLY public.survey_unit
    ADD CONSTRAINT "FKmxuurbuictd8h1b56n700585p" FOREIGN KEY (questionnaire_model_id) REFERENCES public.questionnaire_model(id);

ALTER TABLE ONLY public.questionnaire_model
    ADD CONSTRAINT "FKpps5mdanjpiyufudkdkqyvoh3" FOREIGN KEY (campaign_id) REFERENCES public.campaign(id);


