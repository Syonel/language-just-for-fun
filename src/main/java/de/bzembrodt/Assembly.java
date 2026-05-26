package de.bzembrodt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Assembly {

    private static class ByteReader {
        private final byte[] bytes;
        int offset = 0;

        public ByteReader(byte[] bytes) {
            this.bytes = bytes;
        }

        long[] read(int typeSize, int arraySize) {
            long[] ret = new long[arraySize];
            for (int i = 0; i < arraySize; i++) {
                long val = 0;
                for (int j = 0; j < typeSize; j++) {
                    val |= (long) (bytes[offset++] & 0xFF) << (j * 8);
                }
                ret[i] = val;
            }
            return ret;
        }

        long readByte() {
            byte byte1 = bytes[offset++];
            return byte1 & 0xFF;
        }

        long[] readBytes(int length) {
            long[] ret = new long[length];
            for (int i = 0; i < length; i++) {
                ret[i] = readByte();
            }
            return ret;
        }

        long readWord() {
            byte byte1 = bytes[offset++];
            byte byte2 = bytes[offset++];
            return ((byte2 & 0xFF) << 8 | byte1 & 0xFF);
        }

        long[] readWords(int length) {
            long[] ret = new long[length];
            for (int i = 0; i < length; i++) {
                ret[i] = readWord();
            }
            return ret;
        }

        long readLong() {
            byte byte1 = bytes[offset++];
            byte byte2 = bytes[offset++];
            byte byte3 = bytes[offset++];
            byte byte4 = bytes[offset++];
            return ((long) (byte4 & 0xFF) << 24 | (byte3 & 0xFF) << 16 | (byte2 & 0xFF) << 8 | byte1 & 0xFF);
        }

        long[] readLongs(int length) {
            long[] ret = new long[length];
            for (int i = 0; i < length; i++) {
                ret[i] = readLong();
            }
            return ret;
        }


        void skip(int length) {
            offset += length;
        }

        void skipTo(int offset) {
            this.offset = offset;
        }
    }

    private class Type {
        public static final int BYTE = 1;
        public static final int WORD = 2;
        public static final int LONG = 4;
    }

    private static class Field {
        private final String name;
        private final int typeSize;
        private final int arraySize;

        public Field(String name, int typeSize) {
            this(name, typeSize, 1);
        }

        public Field(String name, int typeSize, int arraySize) {
            this.name = name;
            this.typeSize = typeSize;
            this.arraySize = arraySize;
        }
    }

    //https://offwhitesecurity.dev/malware-development/portable-executable-pe/dos-header/
    private static final List<Field> DOS_HEADER = List.of(
            //typedef struct _IMAGE_DOS_HEADER {                       // DOS .EXE header
            new Field("e_magic", Type.WORD),                     // Magic number
            new Field("e_cblp", Type.WORD),                      // Bytes on last page of file
            new Field("e_cp", Type.WORD),                        // Pages in file
            new Field("e_crlc", Type.WORD),                      // Relocations
            new Field("e_cparhdr", Type.WORD),                   // Size of header in paragraphs
            new Field("e_minalloc", Type.WORD),                  // Minimum extra paragraphs needed
            new Field("e_maxalloc", Type.WORD),                  // Maximum extra paragraphs needed
            new Field("e_ss", Type.WORD),                        // Initial (relative) SS value
            new Field("e_sp", Type.WORD),                        // Initial SP value
            new Field("e_csum", Type.WORD),                      // Checksum
            new Field("e_ip", Type.WORD),                        // Initial IP value
            new Field("e_cs", Type.WORD),                        // Initial (relative) CS value
            new Field("e_lfarlc", Type.WORD),                    // File address of relocation table
            new Field("e_ovno", Type.WORD),                      // Overlay number
            new Field("e_res", Type.WORD, 4),           // Reserved words
            new Field("e_oemid", Type.WORD),                     // OEM identifier (for e_oeminfo)
            new Field("e_oeminfo", Type.WORD),                   // OEM information; e_oemid specific
            new Field("e_res2", Type.WORD, 10),         // Reserved words
            new Field("e_lfanew", Type.LONG)                     // File address of new exe header
            //} IMAGE_DOS_HEADER, *PIMAGE_DOS_HEADER;
    );

    private static final List<Field> COFF_HEADER = List.of(
            new Field("Magic", 4),
            new Field("Machine", 2),
            new Field("NumberOfSections", 2),
            new Field("TimeDateStamp", 4),
            new Field("PointerToSymbolTable", 4),
            new Field("NumberOfSymbols", 4),
            new Field("SizeOfOptionalHeader", 2),
            new Field("Characteristics", 2)
    );
    private static final List<Field> OPTIONAL_HEADER = List.of(
            new Field("Magic", 2),
            new Field("MajorLinkerVersion", 1),
            new Field("MinorLinkerVersion", 1),
            new Field("SizeOfCode", 4),
            new Field("SizeOfInitializedData", 4),
            new Field("SizeOfUninitializedData", 4),
            new Field("AddressOfEntryPoint", 4),
            new Field("BaseOfCode", 4),
            new Field("ImageBase", 8),
            new Field("SectionAlignment", 4),
            new Field("FileAlignment", 4),
            new Field("MajorOperatingSystemVersion", 2),
            new Field("MinorOperatingSystemVersion", 2),
            new Field("MajorImageVersion", 2),
            new Field("MinorImageVersion", 2),
            new Field("MajorSubsystemVersion", 2),
            new Field("MinorSubsystemVersion", 2),
            new Field("Win32VersionValue", 4),
            new Field("SizeOfImage", 4),
            new Field("SizeOfHeaders", 4),
            new Field("CheckSum", 4),
            new Field("Subsystem", 2),
            new Field("DllCharacteristics", 2),
            new Field("SizeOfStackReserve", 8),
            new Field("SizeOfStackCommit", 8),
            new Field("SizeOfHeapReserve", 8),
            new Field("SizeOfHeapCommit", 8),
            new Field("LoaderFlags", 4),
            new Field("NumberOfRvaAndSizes", 4)
    );

    private static final List<Field> DATA_DIRECTORIES = List.of(
            new Field("Export Table (Address)", 4),
            new Field("Export Table (Size)", 4),
            new Field("Import Table (Address)", 4),
            new Field("Import Table (Size)", 4),
            new Field("Resource Table (Address)", 4),
            new Field("Resource Table (Size)", 4),
            new Field("Exception Table (Address)", 4),
            new Field("Exception Table (Size)", 4),
            new Field("Certificate Table (Address)", 4),
            new Field("Certificate Table (Size)", 4),
            new Field("Base Relocation Table (Address)", 4),
            new Field("Base Relocation Table (Size)", 4),
            new Field("Debug (Address)", 4),
            new Field("Debug (Size)", 4),
            new Field("Architecture (Address)", 4),
            new Field("Architecture (Size)", 4),
            new Field("Global Ptr (Address)", 4),
            new Field("Global Ptr (Size)", 4),
            new Field("TLS Table (Address)", 4),
            new Field("TLS Table (Size)", 4),
            new Field("Load Config Table (Address)", 4),
            new Field("Load Config Table (Size)", 4),
            new Field("Bound Import (Address)", 4),
            new Field("Bound Import (Size)", 4),
            new Field("IAT (Address)", 4),
            new Field("IAT (Size)", 4),
            new Field("Delay Import Descriptor (Address)", 4),
            new Field("Delay Import Descriptor (Size)", 4),
            new Field("CLR Runtime Header (Address)", 4),
            new Field("CLR Runtime Header (Size)", 4),
            new Field("Reserved, must be zero (Address)", 4),
            new Field("Reserved, must be zero (Size)", 4)
    );
    private static final List<Field> SECTION = List.of(
            new Field("Name", 8),
            new Field("VirtualSize", 4),
            new Field("VirtualAddress", 4),
            new Field("SizeOfRawData", 4),
            new Field("PointerToRawData", 4),
            new Field("PointerToRelocations", 4),
            new Field("PointerToLinenumbers", 4),
            new Field("NumberOfRelocations", 2),
            new Field("NumberOfLinenumbers", 2),
            new Field("Characteristics", 4)
    );

    private static final List<Field> IMAGE_IMPORT_DESCRIPTOR = List.of(
            new Field("OriginalFirstThunk", 4),
            new Field("TimeDateStamp", 4),
            new Field("ForwarderChain", 4),
            new Field("Name", 4),
            new Field("FirstThunk", 4)
    );

    private static class Section {
        Map<Field, long[]> values;
        String name;
        long virtualSize;
        long virtualAddress;
        long sizeOfRawData;
        long pointerToRawData;

        public Section(Map<Field, long[]> values) {
            this.values = values;
            long nameLong = getFieldValue("Name", SECTION, values);
            name = new String(longToBytes(nameLong), StandardCharsets.UTF_8).replace("\0", "");
            virtualSize = getFieldValue("VirtualSize", SECTION, values);
            virtualAddress = getFieldValue("VirtualAddress", SECTION, values);
            sizeOfRawData = getFieldValue("SizeOfRawData", SECTION, values);
            pointerToRawData = getFieldValue("PointerToRawData", SECTION, values);
        }
    }


    private static final long[] DOS_STUB = {0x0E, 0x1F, 0xBA, 0x0E, 0x00, 0xB4, 0x09, 0xCD, 0x21, 0xB8, 0x01, 0x4C, 0xCD, 0x21, 0x54, 0x68, 0x69, 0x73, 0x20, 0x70, 0x72, 0x6F, 0x67, 0x72, 0x61, 0x6D, 0x20, 0x63, 0x61, 0x6E, 0x6E, 0x6F, 0x74, 0x20, 0x62, 0x65, 0x20, 0x72, 0x75, 0x6E, 0x20, 0x69, 0x6E, 0x20, 0x44, 0x4F, 0x53, 0x20, 0x6D, 0x6F, 0x64, 0x65, 0x2E, 0x0D, 0x0D, 0x0A, 0x24, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x49, 0x36, 0x06, 0xC1, 0x0D, 0x57, 0x68, 0x92, 0x0D, 0x57, 0x68, 0x92, 0x0D, 0x57, 0x68, 0x92, 0x74, 0xD6, 0x69, 0x93, 0x0E, 0x57, 0x68, 0x92, 0x0D, 0x57, 0x69, 0x92, 0x0E, 0x57, 0x68, 0x92, 0x0D, 0x57, 0x68, 0x92, 0x0C, 0x57, 0x68, 0x92, 0x9F, 0xDC, 0x6A, 0x93, 0x0C, 0x57, 0x68, 0x92, 0x52, 0x69, 0x63, 0x68, 0x0D, 0x57, 0x68, 0x92, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final List<Long> CODE = new ArrayList<>();
    private static final List<String> IMPORTED_FUNCTIONS = List.of("GetStdHandle", "WriteConsoleA", "ExitProcess");
    private static final String MESSAGE = "How's it going?\n";

    private static final String DLL_NAME = "KERNEL32.dll";

    static void main() throws IOException {
        readExe();
        writeExe();
    }

    private static void readExe() throws IOException {
        byte[] fileContent = Files.readAllBytes(Path.of("asm/hello.exe"));
        ByteReader byteReader = new ByteReader(fileContent);

        Map<Field, long[]> dosHeaderValues = readFields(DOS_HEADER, byteReader);
        assert 0x5A4D == getFieldValue("e_magic", DOS_HEADER, dosHeaderValues);
        printFields("DOS_HEADER", DOS_HEADER, dosHeaderValues);

        byteReader.skipTo((int) getFieldValue("e_lfanew", DOS_HEADER, dosHeaderValues));

        Map<Field, long[]> coffHeaderValues = readFields(COFF_HEADER, byteReader);
        assert 0x4550 == getFieldValue("Magic", COFF_HEADER, coffHeaderValues);
        assert 0x8664 == getFieldValue("Machine", COFF_HEADER, coffHeaderValues);
        printFields("COFF_HEADER", COFF_HEADER, coffHeaderValues);

        Map<Field, long[]> optionalHeaderValues = readFields(OPTIONAL_HEADER, byteReader);
        assert 0x20B == getFieldValue("Magic", OPTIONAL_HEADER, optionalHeaderValues);
        printFields("OPTIONAL_HEADER", OPTIONAL_HEADER, optionalHeaderValues);

        Map<Field, long[]> dataDirectoriesValues = readFields(DATA_DIRECTORIES, byteReader);
        printFields("DATA_DIRECTORIES", DATA_DIRECTORIES, dataDirectoriesValues);

        assert getFieldValue("SizeOfOptionalHeader", COFF_HEADER, coffHeaderValues) == getSize(OPTIONAL_HEADER) + getSize(DATA_DIRECTORIES);

        long sectionCount = getFieldValue("NumberOfSections", COFF_HEADER, coffHeaderValues);
        List<Section> sections = new ArrayList<>((int) sectionCount);
        for (int i = 0; i < sectionCount; i++) {
            Map<Field, long[]> sectionValues = readFields(SECTION, byteReader);
            Section section = new Section(sectionValues);
            sections.add(section);
            printFields("SECTION " + section.name, SECTION, section.values);
        }

        long importTableAddress = getFieldValue("Import Table (Address)", DATA_DIRECTORIES, dataDirectoriesValues);
        long importTableSize = getFieldValue("Import Table (Size)", DATA_DIRECTORIES, dataDirectoriesValues);
        Section importSection = sections.stream().filter(s -> s.virtualAddress <= importTableAddress && s.virtualAddress + s.virtualSize > importTableAddress).findFirst().orElseThrow();
        long importFileAddress = importTableAddress - importSection.virtualAddress + importSection.pointerToRawData;
        byteReader.skipTo((int) importFileAddress);

        Map<Field, long[]> imageImportDescriptorValues = readFields(IMAGE_IMPORT_DESCRIPTOR, byteReader);
        printFields("IMAGE_IMPORT_DESCRIPTOR", IMAGE_IMPORT_DESCRIPTOR, imageImportDescriptorValues);

        long iltFileAddress = getFieldValue("OriginalFirstThunk", IMAGE_IMPORT_DESCRIPTOR, imageImportDescriptorValues) - importSection.virtualAddress + importSection.pointerToRawData;
        long iatFileAddress = getFieldValue("FirstThunk", IMAGE_IMPORT_DESCRIPTOR, imageImportDescriptorValues) - importSection.virtualAddress + importSection.pointerToRawData;
        long dllNameFileAddress = getFieldValue("Name", IMAGE_IMPORT_DESCRIPTOR, imageImportDescriptorValues) - importSection.virtualAddress + importSection.pointerToRawData;

        String dllName = readString(dllNameFileAddress, byteReader);
        byteReader.skipTo((int) iltFileAddress);
        List<Long> importRvas = new ArrayList<>();
        long v;
        while ((v = byteReader.read(8, 1)[0]) != 0) {
            importRvas.add(v);
        }
        for (Long importRva : importRvas) {
            long fileAddress = importRva - importSection.virtualAddress + importSection.pointerToRawData;
            byteReader.skipTo((int) fileAddress);
            long hint = byteReader.readWord();
            System.out.println("Hint: " + hint);
            System.out.println(readString(fileAddress + 2, byteReader));
        }

        Section textSection = sections.stream().filter(s -> s.name.equals(".text")).findFirst().orElseThrow();
        byteReader.skipTo((int) textSection.pointerToRawData);
        for (int i = 0; i < textSection.virtualSize; ++i) {
            CODE.add(byteReader.read(1, 1)[0]);
        }

        System.out.println(dllName);
    }

    private static String readString(long dllNameFileAddress, ByteReader byteReader) {
        byteReader.skipTo((int) dllNameFileAddress);
        List<Byte> byteList = new ArrayList<>();
        long b;
        while ((b = byteReader.readByte()) != 0) {
            byteList.add((byte) b);
        }
        byte[] bytes = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            bytes[i] = byteList.get(i);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static Map<Field, long[]> readFields(List<Field> fields, ByteReader byteReader) {
        Map<Field, long[]> map = new HashMap<>();
        for (Field field : fields) {
            map.put(field, byteReader.read(field.typeSize, field.arraySize));
        }
        return map;
    }

    private static void writeFields(List<Field> fields, Map<Field, long[]> fieldValues, FileOutputStream outputStream) throws IOException {
        for (Field field : fields) {
            long[] fieldValue = fieldValues.get(field);
            for (int i = 0; i < field.arraySize; i++) {
                long value = 0;
                if (fieldValue != null && fieldValue.length > i) {
                    value = fieldValue[i];
                }
                writeSized(value, field.typeSize, outputStream);
            }
        }
    }

    private static void writeSized(long value, int size, FileOutputStream outputStream) throws IOException {
        for (int i = 0; i < size; i++) {
            outputStream.write(new byte[]{(byte) (value & 0xFF)});
            value >>= 8;
        }
    }

    private static void printFields(String name, List<Field> fields, Map<Field, long[]> values) {
        System.out.println(name + " = {");
        for (Field field : fields) {
            String value = "";
            if (field.arraySize == 1) {
                value = values.get(field)[0] + " (0x" + Long.toHexString(values.get(field)[0]).toUpperCase() + ")";
            } else {
                value = "[" + Arrays.stream(values.get(field)).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + "]";
            }
            System.out.println("   " + field.name + " = " + value);
        }
        System.out.println("}");
    }

    private static long getFieldValue(String name, List<Field> fields, Map<Field, long[]> values) {
        Field field = getField(name, fields);
        return values.get(field)[0];
    }

    private static Field getField(String name, List<Field> fields) {
        return fields.stream().filter(f -> f.name.equals(name)).findFirst().orElseThrow();
    }

    private static int getSize(List<Field> fields) {
        return fields.stream().map(f -> f.typeSize * f.arraySize).reduce(0, Integer::sum);
    }

    private static byte[] longToBytes(long lng) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (lng >> (8 * i));
        }
        return bytes;
    }

    //https://learn.microsoft.com/en-us/windows/win32/debug/pe-format
    //https://0xrick.github.io/win-internals/pe7/
    private static void writeExe() throws IOException {
        File file = Path.of("asm/generated.exe").toFile();
        file.createNewFile();
        long numberOfSections = 3;

        List<Long> code = CODE;
        if (true) {

            code = generateCode();
        }

        Map<Field, long[]> dosHeaderValues = new HashMap<>();
        dosHeaderValues.put(getField("e_magic", DOS_HEADER), new long[]{0x5A4D});
        dosHeaderValues.put(getField("e_cparhdr", DOS_HEADER), new long[]{4});
        dosHeaderValues.put(getField("e_minalloc", DOS_HEADER), new long[]{0x10});
        dosHeaderValues.put(getField("e_maxalloc", DOS_HEADER), new long[]{0xffff});
        dosHeaderValues.put(getField("e_sp", DOS_HEADER), new long[]{0xB8});
        dosHeaderValues.put(getField("e_lfarlc", DOS_HEADER), new long[]{0x40});
        dosHeaderValues.put(getField("e_lfanew", DOS_HEADER), new long[]{0xd0});

        Map<Field, long[]> coffHeaderValues = new HashMap<>();
        coffHeaderValues.put(getField("Magic", COFF_HEADER), new long[]{0x4550});
        coffHeaderValues.put(getField("Machine", COFF_HEADER), new long[]{0x8664});
        coffHeaderValues.put(getField("NumberOfSections", COFF_HEADER), new long[]{numberOfSections});
        coffHeaderValues.put(getField("TimeDateStamp", COFF_HEADER), new long[]{Instant.now().getEpochSecond()});
        coffHeaderValues.put(getField("SizeOfOptionalHeader", COFF_HEADER), new long[]{getSize(OPTIONAL_HEADER) + getSize(DATA_DIRECTORIES)});
        coffHeaderValues.put(getField("Characteristics", COFF_HEADER), new long[]{0x2 | 0x20});//IMAGE_FILE_EXECUTABLE_IMAGE | IMAGE_FILE_LARGE_ADDRESS_AWARE

        Map<Field, long[]> optionalHeaderValues = new HashMap<>();
        optionalHeaderValues.put(getField("Magic", OPTIONAL_HEADER), new long[]{0x20B});//PE32+
        optionalHeaderValues.put(getField("SizeOfCode", OPTIONAL_HEADER), new long[]{0x200});
        optionalHeaderValues.put(getField("SizeOfInitializedData", OPTIONAL_HEADER), new long[]{0x400});
        optionalHeaderValues.put(getField("AddressOfEntryPoint", OPTIONAL_HEADER), new long[]{0x1000});
        optionalHeaderValues.put(getField("BaseOfCode", OPTIONAL_HEADER), new long[]{0x1000});
        optionalHeaderValues.put(getField("ImageBase", OPTIONAL_HEADER), new long[]{0x140000000L});
        optionalHeaderValues.put(getField("SectionAlignment", OPTIONAL_HEADER), new long[]{0x1000});
        optionalHeaderValues.put(getField("FileAlignment", OPTIONAL_HEADER), new long[]{0x200});
        optionalHeaderValues.put(getField("MajorOperatingSystemVersion", OPTIONAL_HEADER), new long[]{0x6}); //Seems to be vista?
        optionalHeaderValues.put(getField("MajorSubsystemVersion", OPTIONAL_HEADER), new long[]{0x6});
        optionalHeaderValues.put(getField("SizeOfImage", OPTIONAL_HEADER), new long[]{0x4000});
        optionalHeaderValues.put(getField("SizeOfHeaders", OPTIONAL_HEADER), new long[]{0x400});
        optionalHeaderValues.put(getField("Subsystem", OPTIONAL_HEADER), new long[]{3});//Console
        optionalHeaderValues.put(getField("DllCharacteristics", OPTIONAL_HEADER), new long[]{0x20 | 0x40 | 0x100 | 0x8000});//IMAGE_DLLCHARACTERISTICS_HIGH_ENTROPY_VA | IMAGE_DLLCHARACTERISTICS_DYNAMIC_BASE | IMAGE_DLLCHARACTERISTICS_NX_COMPAT | IMAGE_DLLCHARACTERISTICS_TERMINAL_SERVER_AWARE
        optionalHeaderValues.put(getField("SizeOfStackReserve", OPTIONAL_HEADER), new long[]{0x100000});
        optionalHeaderValues.put(getField("SizeOfStackCommit", OPTIONAL_HEADER), new long[]{0x1000});
        optionalHeaderValues.put(getField("SizeOfHeapReserve", OPTIONAL_HEADER), new long[]{0x100000});
        optionalHeaderValues.put(getField("SizeOfHeapCommit", OPTIONAL_HEADER), new long[]{0x1000});
        optionalHeaderValues.put(getField("NumberOfRvaAndSizes", OPTIONAL_HEADER), new long[]{DATA_DIRECTORIES.size() / 2});

        long rDataBase = 0x2000;
        Map<Field, long[]> dataDirectoriesValues = new HashMap<>();
        dataDirectoriesValues.put(getField("IAT (Address)", DATA_DIRECTORIES), new long[]{rDataBase});
        long iatSize = (IMPORTED_FUNCTIONS.size() + 1) * 8L;
        dataDirectoriesValues.put(getField("IAT (Size)", DATA_DIRECTORIES), new long[]{iatSize});
        dataDirectoriesValues.put(getField("Import Table (Address)", DATA_DIRECTORIES), new long[]{rDataBase + iatSize});
        long iltSize = getSize(IMAGE_IMPORT_DESCRIPTOR) * 2L;
        dataDirectoriesValues.put(getField("Import Table (Size)", DATA_DIRECTORIES), new long[]{iltSize});

        Map<Field, long[]> textSectionHeaderValues = new HashMap<>();
        textSectionHeaderValues.put(getField("Name", SECTION), new long[]{0x747865742EL});//.text
        textSectionHeaderValues.put(getField("VirtualSize", SECTION), new long[]{code.size()});
        textSectionHeaderValues.put(getField("VirtualAddress", SECTION), new long[]{0x1000});
        textSectionHeaderValues.put(getField("SizeOfRawData", SECTION), new long[]{0x200});
        textSectionHeaderValues.put(getField("PointerToRawData", SECTION), new long[]{0x400});
        textSectionHeaderValues.put(getField("Characteristics", SECTION), new long[]{0x00000020 | 0x20000000 | 0x40000000}); // IMAGE_SCN_CNT_CODE | IMAGE_SCN_MEM_EXECUTE | IMAGE_SCN_MEM_READ

        Map<Field, long[]> rDataSectionHeaderValues = new HashMap<>();
        rDataSectionHeaderValues.put(getField("Name", SECTION), new long[]{0x61746164722EL});//.rdata
        long namesSize = IMPORTED_FUNCTIONS.stream().map(String::length).reduce(0, Integer::sum) + 3L * IMPORTED_FUNCTIONS.size() + DLL_NAME.length() + 1;
        long rdataSize = iatSize * 2 + iltSize + namesSize;
        rDataSectionHeaderValues.put(getField("VirtualSize", SECTION), new long[]{rdataSize});
        rDataSectionHeaderValues.put(getField("VirtualAddress", SECTION), new long[]{rDataBase});
        rDataSectionHeaderValues.put(getField("SizeOfRawData", SECTION), new long[]{0x200});
        rDataSectionHeaderValues.put(getField("PointerToRawData", SECTION), new long[]{0x600});
        rDataSectionHeaderValues.put(getField("Characteristics", SECTION), new long[]{0x00000040 | 0x40000000}); // IMAGE_SCN_CNT_INITIALIZED_DATA | IMAGE_SCN_MEM_READ

        Map<Field, long[]> dataSectionHeaderValues = new HashMap<>();
        dataSectionHeaderValues.put(getField("Name", SECTION), new long[]{0x617461642EL});//.data
        dataSectionHeaderValues.put(getField("VirtualSize", SECTION), new long[]{MESSAGE.length()});
        dataSectionHeaderValues.put(getField("VirtualAddress", SECTION), new long[]{0x3000});
        dataSectionHeaderValues.put(getField("SizeOfRawData", SECTION), new long[]{0x200});
        dataSectionHeaderValues.put(getField("PointerToRawData", SECTION), new long[]{0x800});
        dataSectionHeaderValues.put(getField("Characteristics", SECTION), new long[]{0x00000040 | 0x40000000 | 0x80000000}); // IMAGE_SCN_CNT_INITIALIZED_DATA | IMAGE_SCN_MEM_READ | IMAGE_SCN_MEM_WRITE

        long[] iat = new long[IMPORTED_FUNCTIONS.size() + 1];
        long offset = rDataBase + iatSize * 2 + iltSize;
        for (int i = 0; i < IMPORTED_FUNCTIONS.size(); i++) {
            iat[i] = offset;
            offset += 2 + IMPORTED_FUNCTIONS.get(i).length() + 1;
        }
        iat[iat.length - 1] = 0;

        Map<Field, long[]> imageImportDescriptorValues = new HashMap<>();
        imageImportDescriptorValues.put(getField("OriginalFirstThunk", IMAGE_IMPORT_DESCRIPTOR), new long[]{rDataBase + iatSize + iltSize});
        imageImportDescriptorValues.put(getField("Name", IMAGE_IMPORT_DESCRIPTOR), new long[]{offset});
        imageImportDescriptorValues.put(getField("FirstThunk", IMAGE_IMPORT_DESCRIPTOR), new long[]{rDataBase});

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            writeFields(DOS_HEADER, dosHeaderValues, outputStream);

            writeBytes(DOS_STUB, outputStream);

            writeFields(COFF_HEADER, coffHeaderValues, outputStream);

            writeFields(OPTIONAL_HEADER, optionalHeaderValues, outputStream);

            writeFields(DATA_DIRECTORIES, dataDirectoriesValues, outputStream);

            writeFields(SECTION, textSectionHeaderValues, outputStream);
            writeFields(SECTION, rDataSectionHeaderValues, outputStream);
            writeFields(SECTION, dataSectionHeaderValues, outputStream);


            //Pad to next section
            long headerSize = getSize(DOS_HEADER) + DOS_STUB.length + getSize(COFF_HEADER) + getSize(OPTIONAL_HEADER) + getSize(DATA_DIRECTORIES) + getSize(SECTION) * numberOfSections;
            for (int i = 0; i < 0x400 - headerSize; i++) {
                writeByte(0, outputStream);
            }


            for (long longs : code) {
                writeByte(longs, outputStream);
            }
            for (int i = 0; i < 0x200 - code.size(); ++i) {
                writeByte(0, outputStream);
            }


            for (long l : iat) {
                writeSized(l, 8, outputStream);
            }
            writeFields(IMAGE_IMPORT_DESCRIPTOR, imageImportDescriptorValues, outputStream);
            for (int i = 0; i < getSize(IMAGE_IMPORT_DESCRIPTOR); ++i) {
                writeByte(0, outputStream);
            }
            for (long l : iat) {
                writeSized(l, 8, outputStream);
            }
            for (String importedFunction : IMPORTED_FUNCTIONS) {
                writeSized(0, 2, outputStream); //Hint TODO Should probably load the Dll to provide correct hint
                for (byte b : importedFunction.getBytes(StandardCharsets.UTF_8)) {
                    writeByte(b, outputStream);
                }
                writeByte(0, outputStream);
            }
            for (byte b : DLL_NAME.getBytes(StandardCharsets.UTF_8)) {
                writeByte(b, outputStream);
            }
            writeByte(0, outputStream);

            for (int i = 0; i < 0x200 - rdataSize; ++i) {
                writeByte(0, outputStream);
            }

            for (byte b : MESSAGE.getBytes(StandardCharsets.UTF_8)) {
                writeByte(b, outputStream);
            }
            for (int i = 0; i < 0x200 - MESSAGE.length(); ++i) {
                writeByte(0, outputStream);
            }
        }
    }

    private static List<Long> generateCode() {
        long offset;
        List<Long> code = new ArrayList<>();
        // Prologue
        // Reserve stack space
        code.addAll(List.of(0x48L, 0x83L, 0xECL, 0x28L)); //sub    rsp,0x28

        // Move -11 to first argument
        code.addAll(List.of(0xB9L, 0xF5L, 0xFFL, 0xFFL, 0xFFL));//mov ecx,0FFFFFFFFFFFFFFF5h
        // Call GetStdHandle
        offset = 0x2000 + IMPORTED_FUNCTIONS.indexOf("GetStdHandle") * 8L - 0x1000 - code.size() - 6;
        code.addAll(List.of(0xFFL, 0x15L)); //call offset
        for (int j = 0; j < 4; ++j) {
            code.add(offset & 0xFF);
            offset >>= 8;
        }

        //Push 0 on Stack (as fifth argument, lpReserved)
        code.addAll(List.of(0x48L, 0xC7L, 0x44L, 0x24L, 0x20L, 0x00L, 0x00L, 0x00L, 0x00L)); //mov QWORD PTR [rsp+0x20],0x0
        // Zero out fourth argument, charsWritten
        code.addAll(List.of(0x45L, 0x31L, 0xC9L)); //xor r9d,r9d
        // Load length of string to third argument
        code.addAll(List.of(0x41L, 0xB8L, (long) MESSAGE.length(), 0x00L, 0x00L, 0x00L)); //mov r8d,0Eh
        // Load address of message to second argument
        offset = 0x3000 - 0x1000 - code.size() - 7;
        code.addAll(List.of(0x48L, 0x8DL, 0x15L)); //lea rdx,offset
        System.out.println(offset);
        for (int j = 0; j < 4; ++j) {
            code.add(offset & 0xFF);
            offset >>= 8;
        }

        // Move stdHandle to first argument
        code.addAll(List.of(0x48L, 0x89L, 0xC1L)); //mov rcx,rax
        // Call WriteConsoleA
        offset = 0x2000 + IMPORTED_FUNCTIONS.indexOf("WriteConsoleA") * 8L - 0x1000 - code.size() - 6;
        code.addAll(List.of(0xFFL, 0x15L)); //call offset
        for (int j = 0; j < 4; ++j) {
            code.add(offset & 0xFF);
            offset >>= 8;
        }

        // Exit
        // Zero out first argument
        code.addAll(List.of(0x31L, 0xC9L)); //xor ecx,ecx
        // Call ExitProcess
        offset = 0x2000 + IMPORTED_FUNCTIONS.indexOf("ExitProcess") * 8L - 0x1000 - code.size() - 6;
        code.addAll(List.of(0xFFL, 0x15L)); //call offset
        for (int j = 0; j < 4; ++j) {
            code.add(offset & 0xFF);
            offset >>= 8;
        }

        return code;
    }

    private static void writeByte(long b, FileOutputStream outputStream) throws IOException {
        outputStream.write(new byte[]{(byte) (b & 0xff)});
    }

    private static void writeBytes(long[] bs, FileOutputStream outputStream) throws IOException {
        for (int i = 0; i < bs.length; i++) {
            writeByte(bs[i], outputStream);
        }
    }
}
