const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

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
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profiles or bookmarks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("bookmarks").get());
});

test("Authenticated user: can read and write their own bookmarks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("ceo_dingin").set({
      dramaId: "ceo_dingin",
      title: "CEO Dingin Jatuh Cinta"
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("ceo_dingin").get()
  );
});

test("Cross-user access: Bob cannot read or write Alice's bookmarks", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("ceo_dingin").set({
      dramaId: "ceo_dingin",
      title: "Hacked"
    })
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("bookmarks").doc("ceo_dingin").get()
  );
});

test("Comments: Anyone can read comments, but only signed in users can create comments", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("comments").get());

  await assertFails(
    unauthDb.collection("comments").doc("c1").set({
      userId: "unauth",
      dramaId: "ceo_dingin",
      text: "Test comment",
      createdAt: new Date()
    })
  );

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("comments").doc("c1").set({
      userId: ALICE_UID,
      dramaId: "ceo_dingin",
      text: "Seru banget drama ini!",
      createdAt: new Date()
    })
  );
});
