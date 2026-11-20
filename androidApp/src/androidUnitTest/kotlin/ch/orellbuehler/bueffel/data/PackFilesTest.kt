package ch.orellbuehler.bueffel.data

import ch.orellbuehler.bueffel.ui.addpack.ImportProblem
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PackFilesTest {
    @Test
    fun namesAreSlugsOfTheTitle() {
        assertEquals("spanish-basics.bueffel", packFileName("Spanish Basics"))
        assertEquals("schweizer-kantoene.bueffel", packFileName("Schweizer Kantöne"))
        assertEquals("gruesse.bueffel", packFileName("Grüsse!!"))
        assertEquals("pack.bueffel", packFileName("🇨🇭"))
        assertEquals("pack.bueffel", packFileName(""))
    }

    @Test
    fun longTitlesAreTruncated() {
        val name = packFileName("a".repeat(200))
        assertEquals(48 + ".bueffel".length, name.length)
    }

    @Test
    fun decodesUtf8WithUmlauts() {
        val outcome = decodePackText(ByteArrayInputStream("{\"title\":\"Büffel\"}".toByteArray()))
        assertEquals("{\"title\":\"Büffel\"}", assertIs<ReadOutcome.Text>(outcome).text)
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
