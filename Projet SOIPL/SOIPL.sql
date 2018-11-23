﻿--TODO : GRANT
--TODO : TRIGGER

--TODO : mettre dsd au propre

--TODO : APPLICATION UTILISATEUR
-- ╔ vérifier si utilisateur non désactivé à la connexion (trigger)
-- ╠ ajout tag questions (insert) nam                      
-- ╠ selection questions posées ou répondues (query) nam ┐
-- ╠ selection toutes les questions (query) nam          ├ afficher date, num, utilisateur, date edit, util edit, titre
-- ╠ selection question liée à un tag (query)  nam       ┘
-- ╠ selection d'une question parmi celles affichées (par num ?) + affichage réponses triées (num date auteur score contenu) nam
-- ╠══╦ répondre (max 200 char) nam
-- ║  ╠ voter am (24h pour a, illimité pour m)
-- ║  ╠ editer question ou reponse am
-- ║  ╠ ajout tag am
-- ╠══╩ cloturer question m
-- ╚ reputation augmente → possible changement statut

--TODO : APPLICATION CENTRALE
-- ╔ desactiver compte (enlever connexion)
-- ╠ augmentation forcée de statut 
-- ╠ consulter hitorique utilisateur
-- ╚ ajouter tag.


DROP SCHEMA IF EXISTS SOIPL CASCADE;

-- si on souhaite rajouter des droit en plus pour qu'il puisse faire des create table dans la db 
--GRANT CONNECT ON DATABASE dblbokiau17 to rhonore16;
--GRANT USAGE ON SCHEMA SOIPL TO rhonore16; 	-- entre parentheses donc a voir

--creer des role avant

--GRANT SELECT, INSERT, UPDATE, DELETE ON SOIPL.questions TO dbrhonore16;
-- REVOKE ALL ON SOIPL.questions FROM dbrhonore16; -- pour supprimer son acces a la db

--grant select,insert, update delete on SOIPL.tables to rhonore16
-- grant select on SOIPL.getUser TO rhonore16 => pour les vues.

--GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA SOIPL TO rhonore16;


CREATE SCHEMA SOIPL;


-- est-ce que ça doit etre le plus opti possible ou alors on peut metre un id pour ici la table status meme s’il y a 3 donnees
CREATE TABLE SOIPL.statuts(
	nom_statut VARCHAR(6) CHECK (nom_statut LIKE 'normal' OR nom_statut LIKE 'avancé' OR nom_statut LIKE 'master') PRIMARY KEY,
	seuil INTEGER NOT NULL CHECK (seuil>=0 AND seuil<=100)
);

CREATE TABLE SOIPL.tags(
	id_tag SERIAL PRIMARY KEY, 
	tag VARCHAR(50) NOT NULL CHECK (tag <> '')
);

CREATE TABLE SOIPL.utilisateurs(
	id_utilisateur SERIAL PRIMARY KEY,
	nom_utilisateur VARCHAR(100) NOT NULL CHECK(nom_utilisateur<>'') UNIQUE,
	mot_de_passe VARCHAR(100) NOT NULL CHECK(mot_de_passe<>''),
	email VARCHAR(100) NOT NULL CHECK(email<>'' AND  email like '%_@__%.__%') UNIQUE,
	statut CHAR(6) REFERENCES SOIPL.statuts(nom_statut) NOT NULL DEFAULT 'normal',
	reputation INTEGER NOT NULL DEFAULT 0 CHECK(reputation>=0),
	desactive BOOLEAN NOT NULL DEFAULT FALSE
);

-- quand une question est créée utilisateur créateur est celui qui a crée la question et l'utilisateur edition est mis a null a la création
-- !!!!!! Coherence si utilisateur_edition est à NULL à la creation alors la date_derniere_edition aussi !!!!!!

-- peut on mettre des default NULL ??? ::FAIT -- est-ce qu'une valeur de base est null ? oui
CREATE TABLE SOIPL.questions(
	id_question SERIAL PRIMARY KEY,
	utilisateur_createur INTEGER REFERENCES SOIPL.utilisateurs(id_utilisateur) NOT NULL, 
	date_creation TIMESTAMP NOT NULL,
	utilisateur_edition INTEGER REFERENCES SOIPL.utilisateurs(id_utilisateur) DEFAULT NULL,
	date_derniere_edition TIMESTAMP CHECK(date_derniere_edition > date_creation) DEFAULT NULL,
	texte VARCHAR(255) NOT NULL CHECK(texte<>''),
	titre VARCHAR(100) NOT NULL CHECK(titre<>''),
	cloture BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE SOIPL.question_tag(
	id_ligne_question_tag SERIAL PRIMARY KEY,
	id_question INTEGER REFERENCES SOIPL.questions(id_question),
	id_tag INTEGER REFERENCES SOIPL.tags(id_tag)
);
-- faire un trigger date_heure > questions.date_creation TODO
-- doit etre fait par un utilisateur aussi :: FAIT
CREATE TABLE SOIPL.reponses(
	id_reponse SERIAL PRIMARY KEY,
	id_reponse_par_question INTEGER NOT NULL, -- qd on change de question on passe a 1.
	id_question INTEGER REFERENCES SOIPL.questions(id_question) NOT NULL,
	id_utilisateur INTEGER REFERENCES SOIPL.utilisateurs(id_utilisateur) NOT NULL,
	score INTEGER NOT NULL,
	texte VARCHAR(200) NOT NULL CHECK(texte<>''),
	date_heure TIMESTAMP NOT NULL CHECK(date_heure <= CURRENT_TIMESTAMP)
);

-- un vote doit se reporter a une question : Rajouter question :: FAIT

CREATE TABLE SOIPL.votes(
	id_vote SERIAL PRIMARY KEY,
	id_utilisateur INTEGER REFERENCES SOIPL.utilisateurs(id_utilisateur) NOT NULL,
	positif BOOLEAN NOT NULL,
	date_heure TIMESTAMP NOT NULL CHECK(date_heure <= CURRENT_TIMESTAMP),
	id_reponse INTEGER REFERENCES SOIPL.reponses(id_reponse) NOT NULL
);

INSERT INTO SOIPL.statuts (nom_statut, seuil) VALUES ('normal',0);
INSERT INTO SOIPL.statuts (nom_statut, seuil) VALUES ('avancé',50);
INSERT INTO SOIPL.statuts (nom_statut, seuil) VALUES ('master',100);

INSERT INTO SOIPL.utilisateurs (nom_utilisateur, mot_de_passe, email) VALUES ('leekA','$2a$10$uOyYzAH7RPK98eWRLyYKR.ivX0/VA3j26Kyj6CClIjIgeT6/6nCuC','leekA@gmail.com');
INSERT INTO SOIPL.utilisateurs (nom_utilisateur, mot_de_passe, email) VALUES ('leekB','$2a$10$BYDnAwC4UzTDH.01jVIVy.vacyBxz9zl3mE54x9CppaAyImwdvBTa','leekB@gmail.com');
INSERT INTO SOIPL.utilisateurs (nom_utilisateur, mot_de_passe, email) VALUES ('leekC','$2a$10$HrYEp8gtOWyNpxJsBauzy.psAjzaWvJ5oLiImwow5ahV/cXR6sW8.','leekC@gmail.com');

INSERT INTO SOIPL.tags (tag) VALUES ('questions speciales');

INSERT INTO SOIPL.questions (utilisateur_createur, date_creation, texte, titre) VALUES (1, '12/02/2016 10:10:10', 'while x=0 setWeapon();', 'Est-ce que mon code est opti ?');

INSERT INTO SOIPL.question_tag (id_question, id_tag) VALUES (1,1);

INSERT INTO SOIPL.reponses (id_reponse_par_question,id_question, score, texte, date_heure, id_utilisateur) VALUES (1,1, 20, 'Non il est degueu', '13/02/2016 12:12:12', 2);

INSERT INTO SOIPL.votes (id_utilisateur, positif, date_heure, id_reponse) VALUES (3,true,'13/02/2016 13:13:13',1);

/*DROP TRIGGER changement_dynamique_statut| CREATE TRIGGER changement_dynamique_statut AFTER UPDATE
ON SOIPL.utilisateurs FOR EACH ROW
-- ici ajouter dans le UPDATE le fait de changer le statut si le seuil de reput est dépassé.
UPDATE;
*/

-- Verification que la date des reponses est bien ulterieures a la questions.
CREATE OR REPLACE FUNCTION SOIPL.verif_date_reponses_ulterieures_questions() RETURNS TRIGGER AS $$
DECLARE 
	_id_reponse INTEGER;
BEGIN	
	SELECT COALESCE(MAX(r.id_reponse),0) FROM SOIPL.reponses r
	INTO _id_reponse;
	SELECT COALESCE(r.id_reponse,0) FROM SOIPL.reponses r, SOIPL.questions q WHERE r.id_reponse=_id_reponse AND r.date_heure < q.date_creation
	INTO _id_reponse;
	
	IF _id_reponse <> 0
	THEN
		DELETE FROM SOIPL.votes v WHERE _id_reponse = v.id_reponse;
		DELETE FROM SOIPL.reponses r WHERE _id_reponse = r.id_reponse;
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER verification_date_trigger AFTER INSERT
ON SOIPL.reponses EXECUTE PROCEDURE SOIPL.verif_date_reponses_ulterieures_questions();

INSERT INTO SOIPL.reponses (id_reponse_par_question,id_question, score, texte, date_heure, id_utilisateur) VALUES (2,1, 20, 'Non il est degueu :p', '12/02/2016 09:09:09', 2);

/*Erreur toujours
ERREUR:  une instruction insert ou update sur la table « utilisateurs » viole la contrainte de clé
étrangère « utilisateurs_statut_fkey »
DETAIL:  La clé (statut)=(avance) n'est pas présente dans la table « statuts ».
CONTEXT:  instruction SQL « UPDATE SOIPL.utilisateurs SET statut = _nom_statut WHERE id_utilisateur = NEW.id_utilisateur »

CREATE OR REPLACE FUNCTION SOIPL.statut_maj() RETURNS TRIGGER AS $$
DECLARE
_nom_statut VARCHAR(6);
BEGIN
IF OLD.statut <> 'master'
THEN
	IF NEW.reputation > 50 AND NEW.reputation <100
	THEN 
		_nom_statut = 'avance';
	END IF;
	IF NEW.reputation = 100
	THEN 
		_nom_statut = 'master';
	END IF;
		

	//SELECT COALESCE(s.nom_statut,NULL) FROM SOIPL.statuts s, SOIPL.utilisateurs u
	//WHERE u.id_utilisateur = NEW.id_utilisateur
	//AND u.reputation >= (SELECT MAX(s1.seuil) FROM SOIPL.statuts s1 WHERE s1.seuil < u.reputation)
	//INTO nom_statut;

	IF OLD.statut <> _nom_statut
	THEN
		UPDATE SOIPL.utilisateurs SET statut = _nom_statut WHERE id_utilisateur = NEW.id_utilisateur;
	END IF;
END IF;

RETURN NULL;
END;
$$ LANGUAGE plpgsql;
*/

--DROP TRIGGER statut_maj_trigger ON SOIPL.utilisateurs; A retirer apres
/*CREATE TRIGGER statut_maj_trigger AFTER UPDATE ON SOIPL.utilisateurs FOR EACH ROW
EXECUTE PROCEDURE SOIPL.statut_maj();

UPDATE SOIPL.utilisateurs SET reputation = 60 WHERE id_utilisateur = 2;
*/

-- liste des autres triggers a faire :


-- liste procedure a faire :
/*
	inscription login
	verification pour l'authentification
	insertion de question
	insertion de reponse
	affichage question par tag
	afficahge toutes les questions
	affichage par util.
*/

CREATE OR REPLACE FUNCTION SOIPL.verification_login(VARCHAR,VARCHAR) RETURNS INTEGER AS $$
DECLARE 
	reussi INTEGER;
	_login ALIAS FOR $1;
	_mot_de_passe ALIAS FOR $2;
BEGIN	
	SELECT COALESCE(count(u.id_utilisateur),0) FROM SOIPL.utilisateurs u
	WHERE u.mot_de_passe = _mot_de_passe AND u.nom_utilisateur = _login AND u.desactive = false
	INTO reussi;
	RETURN reussi;
END;
$$ LANGUAGE 'plpgsql';

/*LES AJOUTS*/
-- email puis login puis mdp
CREATE OR REPLACE FUNCTION SOIPL.inscription_utilisateur(VARCHAR(100),VARCHAR(100),VARCHAR(100)) RETURNS INTEGER AS $$
DECLARE 
	_email ALIAS FOR $1;
	_login ALIAS FOR $2;
	_mot_de_passe ALIAS FOR $3;
BEGIN	
	INSERT INTO SOIPL.utilisateurs (nom_utilisateur,mot_de_passe, email) VALUES (_login, _mot_de_passe, _email);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

/*EXEC SOIPL.inscription_utilisateur("test@test.com","login","password");*/
-- regarder comment executer une procedure

CREATE OR REPLACE FUNCTION SOIPL.creation_nouvelle_question(INTEGER,VARCHAR,VARCHAR) RETURNS INTEGER AS $$
DECLARE
	_utilisateur_createur ALIAS FOR $1;
	_texte ALIAS FOR $2;
	_titre ALIAS FOR $3;
BEGIN
	INSERT INTO SOIPL.questions (utilisateur_createur,date_creation,texte,titre) VALUES (_utilisateur_createur,CURRENT_TIMESTAMP,_texte,_titre);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.creation_nouveau_tag(VARCHAR) RETURNS INTEGER AS $$
DECLARE
	_tag ALIAS FOR $1;
BEGIN
	INSERT INTO SOIPL.tags (tag) VALUES (_tag);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

/*CREATE OR REPLACE FUNCTION SOIPL.ajout_tag_question(VARCHAR,INTEGER) RETURNS INTEGER AS $$
DECLARE
	_tag ALIAS FOR $1;
	_question ALIAS FOR $2;
	_nb_question INTEGER;
BEGIN
	SELECT count(*) FROM SOIPL.questions
	INTO _nb_question;

	IF _question < _nb_question AND _question > 0
	THEN
		INSERT INTO SOIPL.question_tag (id_question, id_tag) VALUES (_question,_tag);
	END IF;
END;
$$ LANGUAGE 'plpgsql';
*/
CREATE OR REPLACE FUNCTION SOIPL.creation_vote (INTEGER,BOOLEAN,INTEGER,INTEGER) RETURNS INTEGER AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
	_positif ALIAS FOR $2;
	_id_reponse ALIAS FOR $3;
BEGIN
	INSERT INTO SOIPL.votes (id_utilisateur,positif,date_heure,id_reponse) VALUES (_id_utilisateur,_positif,CURRENT_TIMESTAMP,_id_reponse);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.creation_reponse (INTEGER,VARCHAR) RETURNS INTEGER AS $$
DECLARE
	_id_question ALIAS FOR $1;
	_texte ALIAS FOR $2;
	_num_rep_par_question INTEGER;
BEGIN
	SELECT COALESCE(MAX(id_reponse_par_question),0) FROM SOIPL.reponses WHERE id_question = _id_question
	INTO _num_rep_par_question; 
	INSERT INTO SOIPL.reponses (id_utilisateur,positif,date_heure,id_question,id_reponse_par_question) VALUES (_id_utilisateur,_positif,CURRENT_TIMESTAMP,_id_question,_num_rep_par_question+1);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';
/*LES VUES*/
--CREATE OF REPLACE FUNCTION affichage_toutes_questions RETURNS INTEGER AS $$
-- voir comment return une liste
-- ou alors faite un return view ?? ou return records

CREATE VIEW SOIPL.view_toutes_questions_titre AS
SELECT titre, date_creation, id_question, utilisateur_createur, date_derniere_edition, utilisateur_edition FROM SOIPL.questions ORDER BY date_creation;


/*LES MODIFICATIONS*/

-- a faire des raises.
-- attention il ne faut pas de IF dans les procedures simple !! ==> il faut mettre dans des triggers


/*SELECTIONS DIVERSES*/
CREATE OR REPLACE FUNCTION SOIPL.selection_id_utilisateur_avec_nom_utilisateur (VARCHAR) RETURNS INTEGER AS $$
DECLARE
	_nom_utilisateur ALIAS FOR $1;
	_id_utilisateur INTEGER;
BEGIN
	SELECT COALESCE(id_utilisateur,0) FROM SOIPL.utilisateurs WHERE nom_utilisateur=_nom_utilisateur
	INTO _id_utilisateur;
	RETURN _id_utilisateur;
END;
$$ LANGUAGE 'plpgsql';