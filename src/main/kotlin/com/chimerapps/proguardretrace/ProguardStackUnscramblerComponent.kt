/*
 * Copyright 2019 Chimerapps BVBA & Nicola Verbeeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chimerapps.proguardretrace

import com.intellij.openapi.project.Project
import com.intellij.unscramble.UnscrambleSupport
import proguard.retrace.ReTrace
import java.io.*
import javax.swing.JComponent

class ProguardStackUnscramblerComponent : UnscrambleSupport<JComponent> {

    private companion object {
        private const val REGEX =
            "(?:.*?\\bat\\s+%c\\.%m\\s*\\(%s(?::%l)?\\)\\s*(?:~\\[.*\\])?)|(?:(?:.*?[:\"]\\s+)?%c(?::.*)?)"
    }

    override fun getPresentableName(): String {
        return "Proguard Unscrambler"
    }

    override fun unscramble(project: Project, text: String, logName: String, settings: JComponent?): String? {
        if (logName.isBlank() || !File(logName).exists())
            return null
        if (text.isBlank())
            return ""

        val reader = LineNumberReader(StringReader(text))

        // Open the output stack trace, again using UTF-8 encoding.
        val outputBuffer = StringWriter()
        val writer = PrintWriter(outputBuffer)

        try {
            // Execute ReTrace with the collected settings.
            ReTrace(REGEX, false, File(logName))
                .retrace(reader, writer)
            writer.flush()
            return outputBuffer.toString()
        } finally {
            // Close the input stack trace if it was a file.
            reader.close()
            writer.close()
        }
    }
}