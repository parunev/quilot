-- Create a new database for the application, if you haven't already.
-- It's recommended to use a dedicated database.
CREATE DATABASE IF NOT EXISTS quilot_interviews;

-- Switch to the new database.
USE quilot_interviews;

-- -----------------------------------------------------
-- Table `interviews`
-- This table stores the main record for each interview session.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS interviews (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NULL,
  `interview_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `full_audio` LONGBLOB NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `transcription_entries`
-- This table stores each piece of transcribed text or AI response,
-- linked to a specific interview session.
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS transcription_entries (
  `id` INT NOT NULL AUTO_INCREMENT,
  `interview_id` INT NOT NULL,
  `timestamp` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `speaker` VARCHAR(45) NOT NULL COMMENT 'e.g., \"Interviewer\", \"AI\"',
  `content` TEXT NOT NULL,
  `is_question` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `fk_transcription_entries_interviews_idx` (`interview_id` ASC) VISIBLE,
  CONSTRAINT `fk_transcription_entries_interviews`
    FOREIGN KEY (`interview_id`)
    REFERENCES `interviews` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);

