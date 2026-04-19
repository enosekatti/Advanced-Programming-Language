/*
  Assignment 8: Family Tree in Prolog
  Basic facts: parent/2, male/1, female/1
*/

% --- Individuals (gender) ---

male(arthur).
female(rose).

male(tom).
female(linda).
female(susan).
male(frank).

female(emma).
male(james).
female(mary).

female(sophie).
female(olivia).
male(leo).

% --- parent(Parent, Child) ---

% Arthur and Rose are parents of Tom and Linda
parent(arthur, tom).
parent(rose, tom).
parent(arthur, linda).
parent(rose, linda).

% Tom and Susan are parents of Emma and James
parent(tom, emma).
parent(susan, emma).
parent(tom, james).
parent(susan, james).

% Linda and Frank are parents of Mary
parent(linda, mary).
parent(frank, mary).

% Mary is parent of Sophie and Olivia
parent(mary, sophie).
parent(mary, olivia).

% Emma is parent of Leo
parent(emma, leo).

% ============================================================
% Derived relationships
% ============================================================

% Recursive ancestry: X is an ancestor of Y (any number of generations).
ancestor(X, Y) :- parent(X, Y).
ancestor(X, Y) :- parent(X, Z), ancestor(Z, Y).

% K-generation gap: X is exactly N generations above Y (recursive on N).
relative(X, Y, 1) :- parent(X, Y).
relative(X, Y, N) :-
    integer(N),
    N > 1,
    parent(X, Z),
    N1 is N - 1,
    relative(Z, Y, N1).

% Grandparent: exactly two parent links (recursive rule body).
grandparent(X, Y) :- relative(X, Y, 2).

% Siblings share at least one parent (symmetric; exclude self).
sibling(X, Y) :-
    parent(P, X),
    parent(P, Y),
    X \= Y.

% Cousins: different people whose parents are siblings (and not same person).
cousin(X, Y) :-
    X \= Y,
    parent(Px, X),
    parent(Py, Y),
    Px \= Py,
    sibling(Px, Py),
    \+ sibling(X, Y).

% All descendants: recursive (X is ancestor of Y in the "family line" sense).
descendant(X, Y) :- parent(X, Y).
descendant(X, Y) :- parent(X, Z), descendant(Z, Y).

% Convenience: direct children of a person
child(C, P) :- parent(P, C).

% ============================================================
% Example goals (uncomment in SWI-Prolog or run as queries):
% ?- child(C, tom).
% ?- sibling(X, emma).
% ?- cousin(emma, mary).
% ?- grandparent(arthur, sophie).
% ?- descendant(tom, X).
% ============================================================
