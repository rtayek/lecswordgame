#!/bin/sh
# Architecture guardrails for the Word Guessing Game
# Fails if frozen architectural invariants are violated.
#
# Run from repo root: ./arch-guardrails.sh

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

# 3A) TurnTimer must not leak into view layer
if grep -R "TurnTimer" src/view >/dev/null 2>&1; then
    fail "TurnTimer leaked into view layer"
else
    pass "TurnTimer not referenced by views"
fi

# 3B) Controllers must not publicly expose TurnTimer via fields or return types
if grep -R -n -E '^[[:space:]]*public[[:space:]]+TurnTimer[[:space:]]+[a-zA-Z_][a-zA-Z0-9_]*[[:space:]]*;' src/controller >/dev/null 2>&1; then
    fail "Controller publicly exposes TurnTimer as a field"
fi

if grep -R -n -E '^[[:space:]]*public[[:space:]]+TurnTimer[[:space:]]+[a-zA-Z_][a-zA-Z0-9_]*[[:space:]]*\(' src/controller >/dev/null 2>&1; then
    fail "Controller publicly exposes TurnTimer via a method return type"
fi

pass "Controller does not expose TurnTimer publicly"

# 4) Domain types must not leak into view layer
if grep -R "GamePlayer" src/view >/dev/null 2>&1; then
    fail "Domain type GamePlayer leaked into view"
else
    pass "No domain leakage into view"
fi

# 5) Controllers must be intent-only (no outcome returns)
# This is a heuristic. It flags "return ...Outcome" patterns in controller code.
if grep -R "return .*Outcome" src/controller >/dev/null 2>&1; then
    fail "Controller returns Outcome-like values (UI inference risk)"
else
    pass "Controller API is intent-only (no Outcome returns detected)"
fi

echo
echo "All architecture guardrails passed."
