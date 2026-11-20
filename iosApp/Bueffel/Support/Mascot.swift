import SwiftUI

struct BuffaloHorns: Shape {
    func path(in rect: CGRect) -> Path {
        let scale = min(rect.width, rect.height) / 256
        let originX = rect.midX - 128 * scale
        let originY = rect.midY - 128 * scale
        func point(_ x: CGFloat, _ y: CGFloat) -> CGPoint {
            CGPoint(x: originX + x * scale, y: originY + y * scale)
        }
        var path = Path()
        path.move(to: point(46, 30))
        path.addCurve(to: point(74, 142), control1: point(22, 80), control2: point(34, 126))
        path.addLine(to: point(182, 142))
        path.addCurve(to: point(210, 30), control1: point(222, 126), control2: point(234, 80))
        path.addCurve(to: point(128, 98), control1: point(194, 82), control2: point(160, 98))
        path.addCurve(to: point(46, 30), control1: point(96, 98), control2: point(62, 82))
        path.closeSubpath()
        return path
    }
}

struct HappyEye: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.maxY))
        path.addQuadCurve(
            to: CGPoint(x: rect.maxX, y: rect.maxY),
            control: CGPoint(x: rect.midX, y: 2 * rect.minY - rect.maxY)
        )
        return path
    }
}

struct Mascot: View {
    var size: CGFloat = 160
    var happy = false
    var animated = false
    @State private var lifted = false
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let s = size / 256
        ZStack(alignment: .topLeading) {
            BuffaloHorns()
                .fill(Brand.amber)
                .frame(width: size, height: size)
            RoundedRectangle(cornerRadius: 30 * s, style: .continuous)
                .fill(Brand.violet)
                .frame(width: 124 * s, height: 132 * s)
                .offset(x: 66 * s, y: 100 * s)
            eye(centerX: 105, scale: s)
            eye(centerX: 151, scale: s)
        }
        .frame(width: size, height: size, alignment: .topLeading)
        .offset(y: lifted ? -size * 0.04 : 0)
        .animation(
            animated && !reduceMotion ? Animation.easeInOut(duration: 1.1).repeatForever(autoreverses: true) : nil,
            value: lifted
        )
        .onAppear {
            if animated && !reduceMotion { lifted = true }
        }
        .accessibilityHidden(true)
    }

    @ViewBuilder
    private func eye(centerX: CGFloat, scale s: CGFloat) -> some View {
        if happy {
            HappyEye()
                .stroke(Brand.cream, style: StrokeStyle(lineWidth: 6 * s, lineCap: .round))
                .frame(width: 22 * s, height: 12 * s)
                .offset(x: (centerX - 11) * s, y: 164 * s)
        } else {
            Circle()
                .fill(Brand.cream)
                .frame(width: 22 * s, height: 22 * s)
                .offset(x: (centerX - 11) * s, y: 159 * s)
        }
    }
}
