import SwiftUI

struct JackdawBill: Shape {
    func path(in rect: CGRect) -> Path {
        let scale = min(rect.width, rect.height) / 256
        let originX = rect.midX - 128 * scale
        let originY = rect.midY - 128 * scale
        func point(_ x: CGFloat, _ y: CGFloat) -> CGPoint {
            CGPoint(x: originX + x * scale, y: originY + y * scale)
        }
        var path = Path()
        path.move(to: point(150, 88))
        path.addCurve(to: point(240, 134), control1: point(198, 88), control2: point(228, 110))
        path.addCurve(to: point(150, 168), control1: point(226, 158), control2: point(196, 168))
        path.closeSubpath()
        return path
    }
}

struct JackdawNape: Shape {
    func path(in rect: CGRect) -> Path {
        let scale = min(rect.width, rect.height) / 256
        let originX = rect.midX - 128 * scale
        let originY = rect.midY - 128 * scale
        func point(_ x: CGFloat, _ y: CGFloat) -> CGPoint {
            CGPoint(x: originX + x * scale, y: originY + y * scale)
        }
        var path = Path()
        path.move(to: point(74.4, 65.7))
        for degrees in stride(from: 235.0, through: 125.0, by: -5.0) {
            let angle = degrees * .pi / 180
            path.addLine(to: point(118 + 76 * CGFloat(cos(angle)), 128 + 76 * CGFloat(sin(angle))))
        }
        path.addQuadCurve(to: point(74.4, 65.7), control: point(104, 128))
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
            JackdawBill()
                .fill(Brand.amber)
                .frame(width: size, height: size)
            Circle()
                .fill(Brand.violet)
                .frame(width: 152 * s, height: 152 * s)
                .offset(x: 42 * s, y: 52 * s)
            JackdawNape()
                .fill(Color(hex: 0xC9C4EC))
                .frame(width: size, height: size)
            eye(scale: s)
        }
        .frame(width: size, height: size, alignment: .topLeading)
        .offset(x: -13 * s, y: lifted ? -size * 0.04 : 0)
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
    private func eye(scale s: CGFloat) -> some View {
        if happy {
            HappyEye()
                .stroke(Brand.cream, style: StrokeStyle(lineWidth: 8 * s, lineCap: .round))
                .frame(width: 30 * s, height: 14 * s)
                .offset(x: 127 * s, y: 100 * s)
        } else {
            Circle()
                .fill(Brand.cream)
                .frame(width: 32 * s, height: 32 * s)
                .offset(x: 126 * s, y: 96 * s)
            Circle()
                .fill(Brand.ink)
                .frame(width: 14 * s, height: 14 * s)
                .offset(x: 138 * s, y: 105 * s)
        }
    }
}
