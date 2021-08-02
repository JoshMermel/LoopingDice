load("@rules_cc//cc:defs.bzl", "cc_binary", "cc_library", "cc_test")

cc_library(
    name = "board",
    hdrs = ["board.h"],
    deps = [
        ":enums",
        "@com_google_absl//absl/strings",
    ],
)
cc_test(
    name = "board_test",
    srcs = ["board_test.cc"],
    deps = [
        ":board",
        "@com_google_googletest//:gtest_main",
    ],
)

cc_library(
    name = "enums",
    hdrs = ["enums.h"],
)

cc_test(
    name = "enums_test",
    srcs = ["enums_test.cc"],
    deps = [
        ":enums",
        "@com_google_googletest//:gtest_main",
    ],
)

cc_library(
    name = "moves",
    hdrs = [
	    "bandaged_moves.h",
	    "carousel_moves.h",
	    "gear_moves.h",
	    "moves.h",
	    "lightning_moves.h",
	    "wide_moves.h",
	    ],
    deps = [
      ":board",
    ],
)
cc_test(
    name = "moves_test",
    srcs = ["moves_test.cc"],
    deps = [
        ":board",
        ":moves",
        "@com_google_googletest//:gtest_main",
    ],
)

cc_binary(
    name = "mitm",
    srcs = ["mitm.cc"],
    deps = [
        ":board",
        ":enums",
        ":moves",
        "@com_google_absl//absl/memory:memory",
        "@com_google_absl//absl/strings",
    ],
)

cc_binary(
    name = "bfs",
    srcs = ["bfs.cc"],
    deps = [
        ":board",
        ":enums",
        ":moves",
        "@com_google_absl//absl/memory:memory",
        "@com_google_absl//absl/strings",
    ],
)

cc_binary(
    name = "scramble",
    srcs = ["scramble.cc"],
    deps = [
        ":board",
        ":enums",
        ":moves",
        "@com_google_absl//absl/memory:memory",
        "@com_google_absl//absl/strings",
    ],
)
