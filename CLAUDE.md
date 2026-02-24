# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## About the Project

Java library for reading and writing SIE files — a Swedish open standard for exchanging financial/accounting data between programs. See https://sie.se/in-english/

- **GroupId**: `org.notima`
- **ArtifactId**: `siefilelib`
- **License**: GNU GPL
- **Java target**: 1.8
- **Packaging**: OSGi bundle (via maven-bundle-plugin)

## Build Commands

```bash
# Build
mvn package

# Run all tests (only AllTests.java is included by surefire config)
mvn test

# Run a single test class
mvn test -Dtest=TestVerRec

# Install to local Maven repo
mvn install

# Deploy to Sonatype OSSRH (requires GPG signing)
mvn deploy
```

The test suite entry point is `AllTests.java` — surefire is configured to only run `**/AllTests.java`. To add new test classes, register them in `AllTests.java`'s `@SuiteClasses` annotation.

## Architecture

### Class Hierarchy

`SIEFile` is the base class containing all shared SIE header fields and common data maps. The concrete file type classes extend it:

- **`SIEFileType1`** — Export of year-end balances
- **`SIEFileType2`** — Export of period balances (extends SIEFile directly)
- **`SIEFileType3`** — Export of object balances
- **`SIEFileType4`** — Import/export of transactions (the most feature-complete type)

`SIEFileType4` is the primary type used throughout the codebase.

### Record Classes (`org.notima.sie`)

Each SIE record type has a corresponding Java class:
- **`VerRec`** — Voucher/verification record (`#VER`), contains a list of `TransRec`
- **`TransRec`** — Transaction record (`#TRANS`), can reference a list of `ObjRec` (dimension objects)
- **`AccountRec`** — Account definition (`#KONTO`)
- **`BalanceRec`** — Balance record (`#IB`/`#UB`)
- **`ResRec`** — Result/P&L record (`#RES`)
- **`RARRec`** — Fiscal year record (`#RAR`)
- **`SRURec`** — Tax code record (`#SRU`)
- **`DimRec`** — Dimension record (`#DIM`), standard dimensions: cost center (dim 1), project (dim 6)
- **`ObjRec`** — Object record (`#OBJEKT`), used for cost centers and projects

All record classes implement a `toSieString()` method for serialization back to SIE format.

### Tools (`org.notima.sie.tools`)

- **`SIEFileReader`** — Simple CLI entry point: reads and prints a SIE4 file
- **`SieMerger`** — Merges multiple SIE4 files (from individual files or a directory of `.si`/`.se` files)
- **`SieValidator`** — Validates/normalizes a SIE4 file (e.g., truncates text fields > 100 chars)
- **`SIEFileMergerMain`** / **`SIEFileValidatorMain`** — CLI wrappers for merger and validator

### Converter (`org.notima.sie.converter`)

- **`CsvRecord10`** / **`CsvRecord10Collection`** — CSV format conversion utilities

### Key Implementation Details

- **File encoding**: SIE files use IBM437 (PC8) charset — all reads and writes use `"IBM437"` encoding
- **SIEFile.readFile()**: Two-pass approach — base class parses header fields (`#FLAGGA`, `#FORMAT`, `#SIETYP`, etc.) and stores unrecognized lines in `m_lines`; subclass then iterates `m_lines` to parse type-specific records
- **Dimensions**: `SIEFile` pre-initializes two standard dimensions (cost center dim=1, project dim=6) in `m_dimRecs` and `m_objRecs`
- **`SIEParseException`**: Has severity levels (NORMAL, HIGH, CRITICAL) — only CRITICAL exceptions abort parsing
- **Amount formatting**: Uses `s_amountFormat` (DecimalFormat with `.` as decimal separator) to ensure locale-independent output
- **`validateText()`**: Static helper on `SIEFile` that escapes `"` and strips control characters — called automatically by `setVerText()` and `setTransText()`
- **`TransRec` parsing quirk**: The parser does not correctly handle an empty date placeholder (`""`) when followed by a text field (e.g., `#TRANS 1930 {} 100 "" "Text"`). Omit the date field entirely (`#TRANS 1930 {} 100 "Text"`) when there is no transaction date.
