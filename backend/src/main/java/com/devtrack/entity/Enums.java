package com.devtrack.entity;

/** All enumerations used by the domain model, grouped in one place. */
public final class Enums {
    private Enums() {}

    public enum Priority { LOW, MEDIUM, HIGH }
    public enum TaskStatus { PENDING, IN_PROGRESS, COMPLETED }
    public enum DsaTopic { ARRAYS, STRINGS, LINKED_LISTS, STACKS_AND_QUEUES, HASHING, BINARY_SEARCH,
        SORTING, RECURSION, TREES, GRAPHS, DYNAMIC_PROGRAMMING }
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum ProblemStatus { SOLVED, ATTEMPTED, REVISIT }
    public enum ProjectStatus { PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD }
    public enum NoteCategory { CODING, CONCEPT, INTERVIEW }
}
