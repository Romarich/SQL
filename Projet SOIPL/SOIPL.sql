--TODO : GRANT DONE
--TODO : TRIGGER 

--TODO : APPLICATION UTILISATEUR
-- ╔ FAIT :: vérifier si utilisateur non désactivé à la connexion (trigger) --pense paas finalement
-- ╠ FAIT :: ajout tag questions (insert) nam 
-- ╠ selection questions posées ou répondues (query) nam ┐
-- ╠ FAIT :: selection toutes les questions (query) nam  ├ afficher date, num, utilisateur, date edit, util edit, titre
-- ╠ selection question liée à un tag (query)  nam       ┘
-- ╠ FAIT :: selection d'une question parmi celles affichées (par num ?) + affichage réponses triées (num date auteur score contenu) nam
-- ╠══╦ FAIT :: répondre (max 200 char) nam
-- ║  ╠ FAIT/2 :: voter am (24h pour a, illimité pour m)
-- ║  ╠ FAIT :: editer question ou reponse am
-- ║  ╠ ajout tag am 
-- ╠══╩ FAIT :: cloturer question m 
-- ╚ FAIT :: reputation augmente → possible changement statut 

--TODO : APPLICATION CENTRALE 
-- ╔ FAIT :: desactiver compte (enlever connexion)
-- ╠ FAIT/2 :: augmentation forcée de statut
-- ╠ FAIT :: consulter historique utilisateur
-- ╚ FAIT :: ajouter tag

DROP SCHEMA IF EXISTS SOIPL CASCADE;

CREATE SCHEMA SOIPL;

-- est-ce que ça doit etre le plus opti possible ou alors on peut metre un id pour ici la table status meme s’il y a 3 donnees
CREATE TABLE SOIPL.statuts(
	nom_statut VARCHAR(6) CHECK (nom_statut LIKE 'normal' OR nom_statut LIKE 'avancé' OR nom_statut LIKE 'master') PRIMARY KEY,
	seuil INTEGER NOT NULL CHECK (seuil>=0 AND seuil<=100) UNIQUE
);

CREATE TABLE SOIPL.tags(
	id_tag SERIAL PRIMARY KEY, 
	tag VARCHAR(50) NOT NULL CHECK (tag <> '') UNIQUE
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
-- faire un trigger date_heure > questions.date_creation FAIT
-- doit etre fait par un utilisateur aussi :: FAIT
CREATE TABLE SOIPL.reponses(
	id_reponse SERIAL PRIMARY KEY,
	id_reponse_par_question INTEGER NOT NULL, -- qd on change de question on passe a 1.
	id_question INTEGER REFERENCES SOIPL.questions(id_question) NOT NULL,
	id_utilisateur INTEGER REFERENCES SOIPL.utilisateurs(id_utilisateur) NOT NULL,
	score INTEGER NOT NULL DEFAULT 0,
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
INSERT INTO SOIPL.statuts (nom_statut, seuil) VALUES ('avancé',5);
INSERT INTO SOIPL.statuts (nom_statut, seuil) VALUES ('master',10);

CREATE OR REPLACE FUNCTION SOIPL.statut_maj() RETURNS TRIGGER AS $$
DECLARE
	_seuil_avance INTEGER;
	_seuil_master INTEGER;
BEGIN
IF OLD.statut <> 'master' AND OLD.reputation <> NEW.reputation
THEN
	SELECT seuil FROM SOIPL.statuts WHERE nom_statut LIKE 'master'
	INTO _seuil_master;
	SELECT seuil FROM SOIPL.statuts WHERE nom_statut LIKE 'avancé'
	INTO _seuil_avance;
	IF NEW.reputation >= _seuil_avance AND NEW.reputation < _seuil_master AND OLD.statut = 'normal'
	THEN 
		UPDATE SOIPL.utilisateurs SET statut = 'avancé' WHERE id_utilisateur = NEW.id_utilisateur;
	END IF;
	IF NEW.reputation = _seuil_master AND OLD.statut = 'avancé'
	THEN 
		UPDATE SOIPL.utilisateurs SET statut = 'master' WHERE id_utilisateur = NEW.id_utilisateur;
	END IF;
END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;


--DROP TRIGGER statut_maj_trigger ON SOIPL.utilisateurs; A retirer apres
CREATE TRIGGER statut_maj_trigger AFTER UPDATE ON SOIPL.utilisateurs FOR EACH ROW
EXECUTE PROCEDURE SOIPL.statut_maj();
/*
UPDATE SOIPL.utilisateurs SET reputation = 60 WHERE id_utilisateur = 2;
*/

-- liste des autres triggers a faire :

CREATE OR REPLACE FUNCTION SOIPL.verif_pas_diminution_statut() RETURNS TRIGGER AS $$
DECLARE 
	_statut VARCHAR(6);
BEGIN	
	SELECT statut FROM SOIPL.utilisateurs WHERE id_utilisateur = NEW.id_utilisateur INTO _statut;
	IF _statut = 'master' AND NEW.statut <> 'master'
	THEN
		RAISE 'On ne peut pas diminuer le statut d un utilisateur';
	END IF;
 	IF _statut = 'avancé' AND NEW.statut = 'normal'
	THEN
		RAISE 'On ne peut pas diminuer le statut d un utilisateur';
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';
 CREATE TRIGGER verification_statut_diminution_pas_poss BEFORE UPDATE ON SOIPL.utilisateurs FOR EACH ROW 
EXECUTE PROCEDURE SOIPL.verif_pas_diminution_statut();

-- +5 en cas de vote ++ jamais + 100

CREATE OR REPLACE FUNCTION SOIPL.augmentation_reputation() RETURNS TRIGGER AS $$
DECLARE
	_id_utilisateur INTEGER;
	_reputation INTEGER;
BEGIN

SELECT r.id_utilisateur FROM SOIPL.reponses r WHERE r.id_reponse = NEW.id_reponse
INTO _id_utilisateur;
SELECT u.reputation FROM SOIPL.utilisateurs u WHERE u.id_utilisateur = _id_utilisateur
INTO _reputation;
	IF _reputation<100 AND NEW.positif = true
	THEN
		_reputation = _reputation+5;

		IF _reputation<100
		THEN
			UPDATE SOIPL.utilisateurs SET reputation = _reputation WHERE id_utilisateur = _id_utilisateur;
		ELSE
			UPDATE SOIPL.utilisateurs SET reputation = 100 WHERE id_utilisateur = _id_utilisateur;
		END IF;
	END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER statut_maj_trigger AFTER INSERT ON SOIPL.votes FOR EACH ROW
EXECUTE PROCEDURE SOIPL.augmentation_reputation();

CREATE OR REPLACE FUNCTION SOIPL.augmentation_score_reponses() RETURNS TRIGGER AS $$
DECLARE
	_score INTEGER;
	_positif BOOLEAN;
BEGIN

SELECT r.score FROM SOIPL.reponses r WHERE r.id_reponse = NEW.id_reponse
INTO _score;
	IF NEW.positif = false
	THEN
		IF _score <> 0
		THEN
			_score = _score-1;
			UPDATE SOIPL.reponses SET score = _score WHERE id_reponse = NEW.id_reponse;
		END IF;
		
	ELSE
		_score = _score+1;
		UPDATE SOIPL.reponses SET score = _score WHERE id_reponse = NEW.id_reponse;
	END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER augmentation_score_reponses AFTER INSERT ON SOIPL.votes FOR EACH ROW
EXECUTE PROCEDURE SOIPL.augmentation_score_reponses();

CREATE OR REPLACE FUNCTION SOIPL.util_normal_peut_pas_editer() RETURNS TRIGGER AS $$
DECLARE
	_statut_util VARCHAR(6);
BEGIN

SELECT statut FROM SOIPL.utilisateurs WHERE id_utilisateur = NEW.utilisateur_edition
INTO _statut_util;
	IF _statut_util = 'normal'
	THEN
		RAISE 'Votre statut est trop bas pour pouvoir editer la question';
	END IF;
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER util_normal_peut_pas_editer AFTER UPDATE ON SOIPL.questions FOR EACH ROW
EXECUTE PROCEDURE SOIPL.util_normal_peut_pas_editer();


CREATE OR REPLACE FUNCTION SOIPL.vote_pas_deux_fois() RETURNS TRIGGER AS $$
DECLARE 
	_nb_vote_une_personne INTEGER;
BEGIN	
	SELECT COUNT(*) FROM SOIPL.votes v WHERE v.id_reponse = NEW.id_reponse AND v.id_utilisateur = NEW.id_utilisateur INTO _nb_vote_une_personne;
	IF _nb_vote_une_personne > 1
	THEN 
		RAISE 'Tu ne peux pas voter deux fois pour la meme reponse';
		DELETE FROM SOIPL.votes WHERE id_vote = NEW.id_vote;
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER verif_vote_pas_deux_fois AFTER INSERT ON SOIPL.votes FOR EACH ROW 
EXECUTE PROCEDURE SOIPL.vote_pas_deux_fois();

CREATE OR REPLACE FUNCTION SOIPL.vote_pas_pour_soi_meme() RETURNS TRIGGER AS $$
DECLARE 
	_id_vote INTEGER;
	_id_utilisateur_reponse INTEGER;
	_id_votant INTEGER;
BEGIN	
	SELECT id_utilisateur FROM SOIPL.reponses WHERE id_reponse = NEW.id_reponse INTO _id_utilisateur_reponse;
	SELECT id_utilisateur FROM SOIPL.votes WHERE NEW.id_vote = id_vote INTO _id_votant;

	IF _id_utilisateur_reponse = _id_votant
	THEN 
		SELECT v.id_vote FROM SOIPL.reponses r, SOIPL.votes v WHERE r.id_utilisateur = v.id_utilisateur INTO _id_vote;
		RAISE 'Tu ne peux pas voter pour toi même';
		DELETE FROM SOIPL.votes WHERE id_vote = _id_vote;
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER verification_vote_pas_pour_soi_meme AFTER INSERT ON SOIPL.votes FOR EACH ROW 
EXECUTE PROCEDURE SOIPL.vote_pas_pour_soi_meme();


CREATE OR REPLACE FUNCTION SOIPL.vote_negatif_et_verif_master() RETURNS TRIGGER AS $$
DECLARE 
BEGIN	
	IF EXISTS(SELECT * FROM SOIPL.votes v, SOIPL.utilisateurs u WHERE v.id_vote = NEW.id_vote AND u.id_utilisateur = v.id_utilisateur AND u.statut <> 'master' AND v.positif = false)
	THEN 
		RAISE 'Tu ne peux pas voter negativement car tu n''es pas master';
		DELETE FROM SOIPL.votes v WHERE v.id_vote = NEW.id_vote;
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER vote_negatif_et_verif_master AFTER INSERT ON SOIPL.votes FOR EACH ROW 
EXECUTE PROCEDURE SOIPL.vote_negatif_et_verif_master();

CREATE OR REPLACE FUNCTION SOIPL.peut_voter() RETURNS TRIGGER AS $$
DECLARE
	_id_util INTEGER;
	_statut VARCHAR(6);
	_date_dernier_vote TIMESTAMP;
	_diff INTEGER;
BEGIN	
	SELECT v.id_utilisateur FROM SOIPL.votes v WHERE v.id_vote = NEW.id_vote INTO _id_util;
	SELECT u.statut FROM SOIPL.utilisateurs u WHERE u.id_utilisateur = _id_util INTO _statut;

	IF _statut = 'avancé'
	THEN 
		SELECT COALESCE(MAX(v.date_heure),'2000-01-01T00:00:00') FROM SOIPL.votes v WHERE v.id_utilisateur = _id_util INTO _date_dernier_vote;
		IF age(_date_dernier_vote,now()) < '24 hours'
		THEN
			RAISE 'Vous avez déjà voté trop récemment';
		END IF;
	END IF;

	IF _statut = 'normal' 
	THEN
		RAISE 'Tu ne peux pas voter car tu n as pas un grade assez haut';
	END IF;
	RETURN NULL;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER peut_voter AFTER INSERT ON SOIPL.votes FOR EACH ROW 
EXECUTE PROCEDURE SOIPL.peut_voter();


-- liste des autres triggers a faire :
/*FAIRE LES TRIGGER POUR VERIFIER QUE L UTILISATEUR EST PAS DESACTIVE*/
/*FAIRE LES TRIGGER POUR BLOQUER LA REPUTATION A 100*/
/* +5 POINT QUAND ON VOTE*/

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

/*EXEC SOIPL.inscription_utilisateur('test@test.com','login','password');*/
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



CREATE OR REPLACE FUNCTION SOIPL.ajout_tag_question(VARCHAR,INTEGER) RETURNS INTEGER AS $$
DECLARE
	_tag ALIAS FOR $1;
	_question ALIAS FOR $2;
	_id_tag INTEGER;
	_nombre_tag INTEGER;
BEGIN
	SELECT COUNT(*) FROM SOIPL.question_tag WHERE id_question = _question INTO _nombre_tag;
	IF EXISTS(SELECT id_tag FROM SOIPL.tags WHERE _tag = tag) AND _nombre_tag < 2 AND NOT EXISTS (SELECT qt.id_tag FROM SOIPL.question_tag qt, SOIPL.tags t WHERE _tag = t.tag AND _question = id_question AND t.id_tag = qt.id_tag)
	THEN
		SELECT id_tag FROM SOIPL.tags WHERE _tag = tag INTO _id_tag;
		INSERT INTO SOIPL.question_tag (id_question, id_tag) VALUES (_question,_id_tag);
	ELSE
		RAISE 'Erreur dans l ajout';
	END IF;
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';



CREATE OR REPLACE FUNCTION SOIPL.creation_vote (INTEGER,BOOLEAN,INTEGER) RETURNS INTEGER AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
	_positif ALIAS FOR $2;
	_id_reponse ALIAS FOR $3;
BEGIN
	INSERT INTO SOIPL.votes (id_utilisateur,positif,date_heure,id_reponse) VALUES (_id_utilisateur,_positif,CURRENT_TIMESTAMP,_id_reponse);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.creation_reponse (INTEGER,VARCHAR,INTEGER) RETURNS INTEGER AS $$
DECLARE
	_id_question ALIAS FOR $1;
	_texte ALIAS FOR $2;
	_num_rep_par_question INTEGER;
	_id_utilisateur ALIAS FOR $3;
BEGIN
	SELECT COALESCE(MAX(id_reponse_par_question),0) FROM SOIPL.reponses WHERE id_question = _id_question
	INTO _num_rep_par_question; 
	INSERT INTO SOIPL.reponses (id_reponse_par_question,id_question, texte, date_heure, id_utilisateur) VALUES (_num_rep_par_question+1,_id_question,_texte,CURRENT_TIMESTAMP,_id_utilisateur);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';



/*LES VUES*/
--CREATE OF REPLACE FUNCTION affichage_toutes_questions RETURNS INTEGER AS $$
-- voir comment return une liste
-- ou alors faite un return view ?? ou return records

CREATE OR REPLACE VIEW SOIPL.view_toutes_questions_titre AS
	SELECT  q.titre,
		q.date_creation,
		q.id_question,
		q.utilisateur_createur,
		q.date_derniere_edition,
		q.utilisateur_edition,
		r.id_utilisateur
	FROM SOIPL.questions q, SOIPL.reponses r
	WHERE r.id_question = q.id_question
	ORDER BY q.date_creation;

CREATE OR REPLACE VIEW SOIPL.view_toutes_questions AS 
	SELECT id_question AS "id_question",
		utilisateur_createur AS "utilisateur_createur",
		date_creation AS "date_creation",
		utilisateur_edition AS "utilisateur_edition",
		date_derniere_edition AS "date_derniere_edition",
		texte AS "texte",
		titre AS "titre",
		cloture AS "cloture"
	FROM SOIPL.questions
	ORDER BY date_creation;

CREATE OR REPLACE VIEW SOIPL.view_questions_utilisateurs AS
	SELECT  q.id_question AS "id_question",
		q.utilisateur_createur AS "utilisateur_createur",
		q.date_creation AS "date_creation",
		q.utilisateur_edition AS "utilisateur_edition",
		q.date_derniere_edition AS "date_derniere_edition",
		q.texte AS "texte",
		q.titre AS "titre",
		q.cloture AS "cloture",
		u.nom_utilisateur AS "nom_utilisateur"
	FROM SOIPL.questions q, SOIPL.utilisateurs u 
	WHERE u.id_utilisateur = q.utilisateur_createur
	ORDER BY q.date_creation;

CREATE OR REPLACE VIEW SOIPL.view_reponses_utilisateurs AS
	SELECT	r.id_reponse,
		r.id_reponse_par_question,
		r.id_question,
		r.id_utilisateur,
		r.score,
		r.texte,
		r.date_heure,
		u.nom_utilisateur
	FROM SOIPL.reponses r, SOIPL.utilisateurs u
	WHERE r.id_utilisateur = u.id_utilisateur
	ORDER BY r.score DESC;

CREATE OR REPLACE VIEW SOIPL.view_questions_tags AS
	SELECT  q.id_question AS "id_question",
		q.utilisateur_createur AS "utilisateur_createur",
		q.date_creation AS "date_creation",
		q.utilisateur_edition AS "utilisateur_edition",
		q.date_derniere_edition AS "date_derniere_edition",
		q.texte AS "texte",
		q.titre AS "titre",
		q.cloture AS "cloture",
		t.id_ligne_question_tag AS "id_ligne_question_tag",
		t.id_question AS "id_question_tag",
		t.id_tag AS "id_tag"
	FROM SOIPL.questions q, SOIPL.question_tag t
	WHERE q.id_question = t.id_question
	ORDER BY q.date_creation;

--CREATE OR REPLACE VIEW SOIPL.selection_questions_posees_par_utilisateur(@id_utilisateur INT) AS
--SELECT titre, utilisateur_createur, date_creation, utilisateur_edition, date_derniere_edition FROM SOIPL.questions WHERE @id_utilisateur = utilisateur_createur AND cloture = false ORDER BY date_creation;

--CREATE OR REPLACE VIEW SOIPL.selection_reponses_sur_questions_posees(@id_question_selectionnee INT) AS
--SELECT id_reponse_par_question, id_utilisateur, score, texte, date_heure FROM SOIPL.reponses WHERE id_question = @id_question_selectionnee ORDER BY date_heure; -- date_heure ou alors par id_reponse_par_question

/*LES MODIFICATIONS*/

-- a faire des raises.
-- attention il ne faut pas de IF dans les procedures simple !! ==> il faut mettre dans des triggers
CREATE OR REPLACE FUNCTION SOIPL.edition_question (VARCHAR,INTEGER, INTEGER) RETURNS INTEGER AS $$
DECLARE
	_texte ALIAS FOR $1;
	_id_utilisateur ALIAS FOR $2;
	_id_question ALIAS FOR $3;
	
BEGIN
	UPDATE SOIPL.questions
	SET utilisateur_edition = _id_utilisateur, 
	texte = _texte,
	date_derniere_edition = CURRENT_TIMESTAMP
	WHERE id_question = _id_question;
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.edition_titre_question (VARCHAR,INTEGER, INTEGER) RETURNS INTEGER AS $$
DECLARE
	_titre ALIAS FOR $1;
	_id_utilisateur ALIAS FOR $2;
	_id_question ALIAS FOR $3;
	
BEGIN
	UPDATE SOIPL.questions
	SET utilisateur_edition = _id_utilisateur, 
	titre = _titre,
	date_derniere_edition = CURRENT_TIMESTAMP
	WHERE id_question = _id_question;
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.cloturer_question (INTEGER,INTEGER) RETURNS INTEGER AS $$
DECLARE
	_question ALIAS FOR $1;
	_id_utilisateur ALIAS FOR $2;
	_statut VARCHAR(6);

BEGIN
	SELECT statut FROM SOIPL.utilisateurs WHERE id_utilisateur = _id_utilisateur INTO _statut;
	IF _statut = 'master'
	THEN
		IF TRUE = (SELECT cloture FROM SOIPL.questions WHERE id_question = _question)
		THEN
			RAISE 'Question déjà cloturée';
		ELSE
			UPDATE SOIPL.questions SET cloture = TRUE
			WHERE id_question = _question;
		END IF;
	ELSE

		RAISE 'Vous ne pouvez pas cloturer une question car vous n''etes pas master';
	END IF;
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

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

/*ADMINISTRATEUR*/
CREATE OR REPLACE FUNCTION SOIPL.creation_nouveau_tag(VARCHAR) RETURNS INTEGER AS $$
DECLARE
	_tag ALIAS FOR $1;
BEGIN
	INSERT INTO SOIPL.tags (tag) VALUES (_tag);
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.augmentation_forcee_statut_utilisateur (INTEGER, VARCHAR) RETURNS INTEGER AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
	_status ALIAS FOR $2;
BEGIN
	UPDATE SOIPL.utilisateurs SET statut = _status WHERE id_utilisateur = _id_utilisateur;
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION SOIPL.desactivation_compte_utilisateur(INTEGER) RETURNS INTEGER AS $$
DECLARE
	_id_utilisateur ALIAS FOR $1;
BEGIN
	UPDATE SOIPL.utilisateurs SET desactive = true WHERE id_utilisateur = _id_utilisateur;
	RETURN 1;
END;
$$ LANGUAGE 'plpgsql';

/*
GRANT CONNECT ON DATABASE dblbokiau17 to rhonore16;
GRANT USAGE ON SCHEMA SOIPL TO rhonore16;

GRANT SELECT, INSERT ON TABLE SOIPL.utilisateurs TO rhonore16;
GRANT SELECT, INSERT, UPDATE ON TABLE SOIPL.questions TO rhonore16;
GRANT SELECT, INSERT, UPDATE ON TABLE SOIPL.reponses TO rhonore16;
GRANT SELECT, INSERT, UPDATE ON TABLE SOIPL.votes TO rhonore16;
GRANT SELECT ON TABLE SOIPL.tags TO rhonore16;
GRANT SELECT ON TABLE SOIPL.question_tag TO rhonore16;

GRANT SELECT ON SOIPL.view_toutes_questions_titre TO rhonore16;
GRANT SELECT ON SOIPL.view_toutes_questions TO rhonore16;
GRANT SELECT ON SOIPL.view_questions_utilisateurs TO rhonore16;
GRANT SELECT ON SOIPL.view_reponses_utilisateurs TO rhonore16;
GRANT SELECT ON SOIPL.view_questions_tags TO rhonore16;

GRANT USAGE, SELECT ON SEQUENCE SOIPL.utilisateurs_id_utilisateur_seq TO rhonore16;
GRANT USAGE, SELECT ON SEQUENCE SOIPL.votes_id_vote_seq TO rhonore16;
GRANT USAGE, SELECT ON SEQUENCE SOIPL.reponses_id_reponse_seq TO rhonore16;
GRANT USAGE, SELECT ON SEQUENCE SOIPL.questions_id_question_seq TO rhonore16;
GRANT USAGE, SELECT ON SEQUENCE SOIPL.tags_id_tag_seq TO rhonore16;
*/