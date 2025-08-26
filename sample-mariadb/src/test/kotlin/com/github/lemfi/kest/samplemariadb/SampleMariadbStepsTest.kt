package com.github.lemfi.kest.samplemariadb

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.json.cli.toJsonString
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.mariadb.cli.cleanMariaDBDatabase
import com.github.lemfi.kest.mariadb.cli.readMariaDBDatabase
import com.github.lemfi.kest.mariadb.cli.updateMariaDBDatabase
import org.junit.jupiter.api.TestFactory
import org.testcontainers.containers.MariaDBContainer

class SampleMariadbStepsTest {

    @TestFactory
    fun `prepare my starwars movies database`() = playScenario {

        val connectionString = step("start mariaDB container") {
            MariaDBContainer("mariadb:11.8.2")
                .withDatabaseName("starwars")
                .apply { start() }
                .jdbcUrl
        }

        nestedScenario("prepare data") {
            nestedScenario("create tables") {
                listOf(
                    """
                    CREATE TABLE IF NOT EXISTS planets (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100),
                        region VARCHAR(100)
                    );
                    """, """
                   CREATE TABLE IF NOT EXISTS characters (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100),
                        species VARCHAR(100),
                        affiliation VARCHAR(100),
                        planet_id INT,
                        FOREIGN KEY (planet_id) REFERENCES planets(id)
                    );
                    """, """
                    CREATE TABLE IF NOT EXISTS starships (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100),
                        model VARCHAR(100),
                        pilot_id INT,
                        FOREIGN KEY (pilot_id) REFERENCES characters(id)
                    );
                    """, """
                    CREATE TABLE IF NOT EXISTS missions (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        code_name VARCHAR(100),
                        objective TEXT,
                        assigned_to INT,
                        FOREIGN KEY (assigned_to) REFERENCES characters(id)
                    );
                    """, """
                    CREATE TABLE IF NOT EXISTS mission_logs (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        mission_id INT,
                        starship_id INT,
                        log_date DATE,
                        outcome TEXT,
                        FOREIGN KEY (mission_id) REFERENCES missions(id),
                        FOREIGN KEY (starship_id) REFERENCES starships(id)
                    );
                    """
                ).forEach {
                    updateMariaDBDatabase {
                        connection = connectionString()
                        sql = it
                    }
                }
            }

            nestedScenario("insert data") {
                listOf(
                    """
                    INSERT INTO planets (name, region) VALUES
                    ('Tatooine', 'Outer Rim'),
                    ('Alderaan', 'Core Worlds'),
                    ('Naboo', 'Mid Rim'),
                    ('Coruscant', 'Core Worlds'),
                    ('Kamino', 'Wild Space'),
                    ('Hoth', 'Outer Rim'),
                    ('Endor', 'Outer Rim'),
                    ('Dagobah', 'Outer Rim'),
                    ('Mustafar', 'Outer Rim'),
                    ('Geonosis', 'Outer Rim');
                    """, """
                    INSERT INTO characters (name, species, affiliation, planet_id) VALUES
                    ('Luke Skywalker', 'Human', 'Rebel Alliance', 1),
                    ('Leia Organa', 'Human', 'Rebel Alliance', 2),
                    ('Darth Vader', 'Human', 'Galactic Empire', 4),
                    ('Obi-Wan Kenobi', 'Human', 'Jedi Order', 3),
                    ('Yoda', 'Unknown', 'Jedi Order', 8),
                    ('Boba Fett', 'Human (Clone)', 'Bounty Hunter', 5),
                    ('Padmé Amidala', 'Human', 'Galactic Republic', 3),
                    ('Anakin Skywalker', 'Human', 'Jedi Order', 1),
                    ('Han Solo', 'Human', 'Rebel Alliance', 1),
                    ('Lando Calrissian', 'Human', 'Rebel Alliance', 4);
                    """, """
                    INSERT INTO starships (name, model, pilot_id) VALUES
                    ('X-Wing', 'T-65B', 1),
                    ('TIE Fighter', 'Twin Ion Engine', 3),
                    ('Millennium Falcon', 'YT-1300', 9),
                    ('Slave I', 'Firespray-31', 6),
                    ('Jedi Interceptor', 'Eta-2', 4),
                    ('Star Destroyer', 'Imperial I-class', 3),
                    ('A-Wing', 'RZ-1', 2),
                    ('Naboo N-1', 'Starfighter', 7),
                    ('Lambda Shuttle', 'T-4a', 10),
                    ('Y-Wing', 'BTL-A4', 1);
                    """, """
                    INSERT INTO missions (code_name, objective, assigned_to) VALUES
                    ('Operation Binary Moon', 'Destroy Imperial relay station', 1),
                    ('Ghost Signal', 'Spy on Imperial fleet', 2),
                    ('Blackout Strike', 'Eliminate Sith target', 4),
                    ('Rebel Rising', 'Recruit allies on Endor', 9),
                    ('Frozen Watch', 'Scout Imperial activity on Hoth', 5),
                    ('Dark Reign', 'Reassert control over Mustafar', 3),
                    ('Hope Spark', 'Smuggle supplies to the Resistance', 10),
                    ('Echo Trap', 'Ambush bounty hunters', 8),
                    ('Council Whisper', 'Attend secret Jedi meeting', 5),
                    ('Nebula Flare', 'Disable enemy communications array', 6);
                    """, """
                    INSERT INTO mission_logs (mission_id, starship_id, log_date, outcome) VALUES
                    (1, 1, '2025-06-01', 'Success'),
                    (2, 7, '2025-06-02', 'Partial success'),
                    (3, 5, '2025-06-03', 'Target eliminated'),
                    (4, 3, '2025-06-04', 'Allies recruited'),
                    (5, 4, '2025-06-05', 'Imperials avoided'),
                    (6, 2, '2025-06-06', 'Control regained'),
                    (7, 9, '2025-06-07', 'Supplies delivered'),
                    (8, 6, '2025-06-08', 'Bounty hunters defeated'),
                    (9, 5, '2025-06-09', 'Meeting completed'),
                    (10, 10, '2025-06-10', 'Array destroyed');

                    """
                ).forEach {
                    updateMariaDBDatabase {
                        connection = connectionString()
                        sql = it
                    } assertThat {
                        it isEqualTo 10
                    }
                }
            }
        }

        val data = readMariaDBDatabase {
            connection = connectionString()
            sql = """SELECT
                    m.code_name AS mission_name,
                    ml.log_date,
                    s.name AS starship_name,
                    s.model AS starship_model,
                    ml.outcome
                FROM mission_logs ml
                JOIN missions m ON ml.mission_id = m.id
                JOIN starships s ON ml.starship_id = s.id
                JOIN characters c ON s.pilot_id = c.id
                WHERE c.name = 'Han Solo';
                """
        } assertThat { resultset ->
            resultset.size isEqualTo 1
            resultset[0].keys isEqualTo setOf("mission_name", "starship_model", "log_date", "starship_name", "outcome")
            resultset[0]["mission_name"] isEqualTo "Rebel Rising"
            resultset[0]["starship_model"] isEqualTo "YT-1300"
            resultset[0]["starship_name"] isEqualTo "Millennium Falcon"
            resultset[0]["outcome"] isEqualTo "Allies recruited"
        }

        step("log result") { it.info(data().toJsonString()) }

        cleanMariaDBDatabase("clean DB") {
            connection = connectionString()
        }
    }
}