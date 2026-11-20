import Foundation
import SwiftUI

extension StyledText {
    var attributed: AttributedString {
        var result = AttributedString()
        for span in spans {
            var part = AttributedString(span.text)
            var intent = InlinePresentationIntent()
            if span.bold { intent.insert(.stronglyEmphasized) }
            if span.italic { intent.insert(.emphasized) }
            if span.code { intent.insert(.code) }
            if !intent.isEmpty {
                part.inlinePresentationIntent = intent
            }
            result.append(part)
        }
        return result
    }
}

struct RichTextView: View {
    let text: StyledText
    var font: Font = .body
    var color: Color = Brand.text
    var alignment: TextAlignment = .leading

    var body: some View {
        Text(text.attributed)
            .font(font)
            .foregroundStyle(color)
            .multilineTextAlignment(alignment)
            .fixedSize(horizontal: false, vertical: true)
    }
}
