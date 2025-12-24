#!/bin/sh
# Architecture guardrails for the Word Guessing Game
# Fails if frozen architectural invariants are violated.

set -eu

fail() {
    echo "❌ $1"
    exit 1
}

pass() {
    echo "✅ $1"
}

echo "Running architecture guardrails…"
echo

# 1) View layer must not import domain/model code
if grep -R "import model\." src/view >/dev/null 2>&1; then
    fail "View layer imports model.*"
else
    pass "View layer does not import model.*"
fi

# 2) Swing must not leak outside view layer
if grep -R "SwingUtilities" src | grep -v "src/view" >/dev/null 2>&1; then
    fail "SwingUtilities used outside view layer"
else
    pass "SwingUtilities isolated to view layer"
fi

# 3) Controllers must not expose timers
if grep -R "TurnTimer" src/controller >/dev/null 2>&1; then
    fail "Controller references TurnTimer"
else
    pass "Controller does not expose timers"
fi

# 4) Domain types must not leak into view layer
if grep -R "GamePlayer" src/view >/dev/null 2>&1; then
    fail "Domain type GamePlayer leaked into view"
else
    pass "No domain leakage into view"
fi

# 5) Controllers must be intent-only (no outcome returns)
if grep -R "return .*Outcome" src/controller >/dev/null 2>&1; then
    fail "Controller returns Outcome-like values"
else
    pass "Controller API is intent-only"
fi

echo
echo "All architecture guardrails passed."
