const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_1";
const BOB_UID = "bob_uid_2";
const ADMIN_UID = "kabir_admin_uid";
const ADMIN_TOKEN = { email: "kabir020314@gmail.com", email_verified: true };
const SPOOF_ADMIN_TOKEN = { email: "kabir020314@gmail.com", email_verified: false };

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("community_posts").where("visibility", "==", "public").get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauth.collection("community_posts").doc("post_1").set({
      userId: ALICE_UID,
      authorName: "Alice",
      title: "Hello",
      body: "Test",
      category: "Tech",
      visibility: "public",
      likesCount: 0,
      repliesCount: 0,
      reported: false,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("User profile, saved_videos, video_notes, and watch_history enforce owner isolation", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Rahman",
      preferredLanguage: "bn",
      favoriteCategories: ["Tech Tutorials", "Bangla Stories"],
      xpPoints: 50,
      streakDays: 2,
      watchedCount: 3,
      watchMinutes: 42,
      badges: ["First Watch"],
      premiumMember: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Ghost field rejected
  await assertFails(
    alice.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Spoof",
      preferredLanguage: "bn",
      xpPoints: 0,
      streakDays: 0,
      watchedCount: 0,
      watchMinutes: 0,
      premiumMember: false,
      isAdmin: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user read & write rejected
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").doc(ALICE_UID).update({ xpPoints: 999 }));

  // Subcollections
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("saved_videos").doc("vid_1").set({
      userId: ALICE_UID,
      videoId: "vid_1",
      title: "Kotlin Flow Guide",
      category: "Tech Tutorials",
      savedAt: now,
    })
  );
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("saved_videos").get());
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("saved_videos").get());

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("video_notes").doc("note_1").set({
      userId: ALICE_UID,
      videoId: "vid_1",
      videoTitle: "Kotlin Flow Guide",
      content: "Remember StateFlow vs SharedFlow",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("video_notes").doc("note_1").update({
      content: "Updated note text",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("video_notes").get());

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("watch_history").doc("vid_1").set({
      userId: ALICE_UID,
      videoId: "vid_1",
      title: "Kotlin Flow Guide",
      playlistId: "pl_android",
      progressPercent: 65,
      lastWatchedAt: now,
    })
  );
});

test("Community posts allow public filtered list, owner edits, likes/reports, and block unfiltered list", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  await assertSucceeds(
    alice.collection("community_posts").doc("post_1").set({
      userId: ALICE_UID,
      authorName: "Alice",
      title: "Android Studio Tips?",
      body: "How to speed up Gradle?",
      category: "Tech Tutorials",
      visibility: "public",
      likesCount: 0,
      repliesCount: 0,
      reported: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Filtered list succeeds; unfiltered fails
  await assertSucceeds(alice.collection("community_posts").where("visibility", "==", "public").get());
  await assertFails(alice.collection("community_posts").get());

  // Bob can like or report, cannot hijack title
  await assertSucceeds(
    bob.collection("community_posts").doc("post_1").update({
      likesCount: 1,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    bob.collection("community_posts").doc("post_1").update({
      reported: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    bob.collection("community_posts").doc("post_1").update({
      title: "Hijacked",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Admin features require verified admin email and block spoofed unverified email", async () => {
  const verifiedAdmin = testEnv.authenticatedContext(ADMIN_UID, ADMIN_TOKEN).firestore();
  const spoofAdmin = testEnv.authenticatedContext("spoof_uid", SPOOF_ADMIN_TOKEN).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 2000);

  const announcementPayload = {
    titleBn: "নতুন ভিডিও!",
    titleEn: "New Video!",
    bodyBn: "আজ রাত ৮টায় লাইভ।",
    bodyEn: "Live tonight at 8 PM.",
    active: true,
    visibility: "public",
    createdAt: now,
    updatedAt: now,
  };

  await assertFails(alice.collection("announcements").doc("ann_1").set(announcementPayload));
  await assertFails(spoofAdmin.collection("announcements").doc("ann_1").set(announcementPayload));
  await assertSucceeds(verifiedAdmin.collection("announcements").doc("ann_1").set(announcementPayload));

  // Poll creation by admin + atomic vote by Alice
  await assertSucceeds(
    verifiedAdmin.collection("polls").doc("poll_1").set({
      questionBn: "পরের ভিডিও কোন বিষয়ে চান?",
      questionEn: "Next video topic?",
      options: ["AI Coding", "Dhaka Tech Story"],
      voteCounts: [0, 0],
      visibility: "public",
      active: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  const batch = alice.batch();
  batch.set(alice.collection("polls").doc("poll_1").collection("votes").doc(ALICE_UID), {
    userId: ALICE_UID,
    optionIndex: 0,
    createdAt: now,
  });
  batch.update(alice.collection("polls").doc("poll_1"), {
    voteCounts: [1, 0],
    updatedAt: new Date(Date.now() - 500),
  });
  await assertSucceeds(batch.commit());
});

test("PII collections (community_joins, video_requests) isolate reads to owner and admin", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const verifiedAdmin = testEnv.authenticatedContext(ADMIN_UID, ADMIN_TOKEN).firestore();
  const now = new Date(Date.now() - 2000);

  await assertSucceeds(
    alice.collection("community_joins").doc("join_1").set({
      userId: ALICE_UID,
      fullName: "Alice Rahman",
      contactInfo: "+8801700000000",
      interests: ["Tech Tutorials"],
      createdAt: now,
    })
  );

  await assertFails(bob.collection("community_joins").doc("join_1").get());
  await assertSucceeds(alice.collection("community_joins").where("userId", "==", ALICE_UID).get());
  await assertSucceeds(verifiedAdmin.collection("community_joins").doc("join_1").get());
});
