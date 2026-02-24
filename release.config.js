module.exports = {
  branches: ["fix/semantic-release-node-version"],  //the main branch only will trigger releases
  plugins: [
    "@semantic-release/commit-analyzer",
    "@semantic-release/release-notes-generator",
    "@semantic-release/changelog",
    "@semantic-release/github",
    "@semantic-release/git"
  ]
};