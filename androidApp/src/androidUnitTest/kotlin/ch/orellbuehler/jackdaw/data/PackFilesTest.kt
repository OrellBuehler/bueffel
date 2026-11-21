package ch.orellbuehler.jackdaw.data

import ch.orellbuehler.jackdaw.ui.addpack.ImportProblem
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PackFilesTest {
    @Test
    fun namesAreSlugsOfTheTitle() {
        assertEquals("spanish-basics.jackdaw", packFileName("Spanish Basics"))
        assertEquals("schweizer-kantoene.jackdaw", packFileName("Schweizer Kantöne"))
        assertEquals("gruesse.jackdaw", packFileName("Grüsse!!"))
        assertEquals("pack.jackdaw", packFileName("🇨🇭"))
        assertEquals("pack.jackdaw", packFileName(""))
    }

    @Test
    fun longTitlesAreTruncated() {
        val name = packFileName("a".repeat(200))
        assertEquals(48 + ".jackdaw".length, name.length)
    }

    @Test
    fun decodesUtf8WithUmlauts() {
        val outcome = decodePackText(ByteArrayInputStream("{\"title\":\"Jackdaw\"}".toByteArray()))
        assertEquals("{\"title\":\"Jackdaw\"}", assertIs<ReadOutcome.Text>(outcome).text)
    }

    @Test
    fun keepsABomForTheSdkToHandle() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "{}".toByteArray()
        assertEquals("﻿{}", assertIs<ReadOutcome.Text>(decodePackText(ByteArrayInputStream(bytes))).text)
    }

    @Test
    fun rejectsInvalidUtf8() {
        val outcome = decodeUtf8(byteArrayOf(0x7B, 0xC3.toByte(), 0x28, 0x7D))
        assertEquals(ImportProblem.NotUtf8, assertIs<ReadOutcome.Failed>(outcome).problem)
    }
}
