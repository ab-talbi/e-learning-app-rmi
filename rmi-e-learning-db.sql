-- phpMyAdmin SQL Dump
-- version 5.1.3
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3310
-- Généré le : mar. 31 jan. 2023 à 19:13
-- Version du serveur : 10.4.24-MariaDB
-- Version de PHP : 8.1.4

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

--
-- Base de données : `rmi-e-learning-db`
--
CREATE DATABASE IF NOT EXISTS `rmi-e-learning-db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `rmi-e-learning-db`;

-- --------------------------------------------------------

--
-- Structure de la table `classes`
--

DROP TABLE IF EXISTS `classes`;
CREATE TABLE `classes` (
  `nom_classe` varchar(50) NOT NULL,
  `nom_prof_associe` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Déchargement des données de la table `classes`
--

INSERT INTO `classes` (`nom_classe`, `nom_prof_associe`) VALUES
('classe1', 'prof1');

-- --------------------------------------------------------

--
-- Structure de la table `registration`
--

DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
  `nom_utilisateur` varchar(50) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `mot_de_passe` varchar(500) NOT NULL,
  `role` varchar(50) NOT NULL,
  `classe` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Déchargement des données de la table `registration`
--

INSERT INTO `registration` (`nom_utilisateur`, `nom`, `prenom`, `mot_de_passe`, `role`, `classe`) VALUES
('admin', 'admin', 'admin', 'd033e22ae348aeb5660fc2140aec35850c4da997', 'Admin', ''),
('ahmed', 'ahmed', 'ahmed', '1698c2bea6c8000723d5bb70363a8352d846917e', 'Etudiant', 'classe1'),
('ayoub', 'ayoub', 'ayoub', '132b19de24d66d2ab7d33b39bcf84bb222b9618e', 'Etudiant', 'classe1'),
('prof1', 'prof1', 'prof1', '7468d258b811d2fd3be09a3221a74766737a560b', 'Professeur', 'classe1');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `classes`
--
ALTER TABLE `classes`
  ADD PRIMARY KEY (`nom_classe`);

--
-- Index pour la table `registration`
--
ALTER TABLE `registration`
  ADD PRIMARY KEY (`nom_utilisateur`);
COMMIT;
