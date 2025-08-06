package com.quilot.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link QuestionDetector} class.
 */
class QuestionDetectorTest {

    private QuestionDetector detector;

    @BeforeEach
    void setUp() {
        detector = new QuestionDetector();
    }

    @Test
    @DisplayName("Should return true for sentences ending with a question mark")
    void isQuestion_EndsWithQuestionMark_ReturnsTrue() {
        assertTrue(detector.isQuestion("What is your name?", "en-US"));
        assertTrue(detector.isQuestion("This is a statement?", "en-US"));
    }

    @Test
    @DisplayName("Should return true for sentences starting with an interrogative word")
    void isQuestion_StartsWithInterrogative_ReturnsTrue() {
        assertTrue(detector.isQuestion("What is the capital of Bulgaria", "en-US"), "Failed on 'What'");
        assertTrue(detector.isQuestion("How do you test this", "en-US"), "Failed on 'How'");
        assertTrue(detector.isQuestion("Can you explain OOP", "en-US"), "Failed on 'Can'");
    }

    @Test
    @DisplayName("Should return true for sentences starting with a command verb")
    void isQuestion_StartsWithCommandVerb_ReturnsTrue() {
        assertTrue(detector.isQuestion("Explain the concept of polymorphism", "en-US"), "Failed on 'Explain'");
        assertTrue(detector.isQuestion("Describe your last project", "en-US"), "Failed on 'Describe'");
        assertTrue(detector.isQuestion("Tell me about a time you failed", "en-US"), "Failed on 'Tell'");
    }

    @Test
    @DisplayName("Should return false for standard declarative sentences")
    void isQuestion_IsDeclarativeSentence_ReturnsFalse() {
        assertFalse(detector.isQuestion("This is a simple statement.", "en-US"));
        assertFalse(detector.isQuestion("I went to the store.", "en-US"));
    }

    @Test
    @DisplayName("Should return false for null, empty, or whitespace-only input")
    void isQuestion_WithInvalidInput_ReturnsFalse() {
        assertFalse(detector.isQuestion(null, "en-US"), "Failed on null input");
        assertFalse(detector.isQuestion("", "en-US"), "Failed on empty string");
        assertFalse(detector.isQuestion("   ", "en-US"), "Failed on whitespace only");
    }

    @Test
    @DisplayName("Should correctly use Bulgarian interrogative words")
    void isQuestion_WithBulgarianLanguage_ReturnsTrue() {
        assertTrue(detector.isQuestion("Какво е това", "bg-BG"), "Failed on 'Какво'");
        assertTrue(detector.isQuestion("Обясни ми за проекта", "bg-BG"), "Failed on 'Обясни'");
    }

    @Test
    @DisplayName("Should return false for unsupported language codes")
    void isQuestion_WithUnsupportedLanguage_ReturnsFalse() {
        // This will return false because "de-DE" is not in our map, but it should still handle the question mark.
        assertFalse(detector.isQuestion("Wie geht es Ihnen", "de-DE"));
        assertTrue(detector.isQuestion("Wie geht es Ihnen?", "de-DE"));
    }
}
